/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.connection.ssh;

import com.dbn.common.util.Commons;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker;
import org.apache.sshd.client.session.forward.PortForwardingTracker;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.common.util.security.SecurityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static com.dbn.connection.ssh.SshAuthType.KEY_PAIR;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@Getter
public class SshTunnelConnector {
    private final SshTunnelConfig config;

    private final String localHost = "localhost";
    private int localPort;
    private ClientSession session;
    private SshClient client;
    private PortForwardingTracker tracker;

    public SshTunnelConnector(SshTunnelConfig config) {
        this.config = config;
    }

    public ClientSession connect() throws Exception {
        initPort();
        initClient();
        initSession();
        initAuth();
        initTracker();
        return session;
    }

    private void initPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            localPort = serverSocket.getLocalPort();
        }
        log.info("SSH Tunnel Connection - Local port initialised as {}", localPort);
    }

    private void initClient() {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier((clientSession, remoteAddress, serverKey) -> true); // Disable host key checking (for development/testing)
        client.start();
        log.info("SSH Tunnel Connection - client initialized");
    }

    private void initSession() throws Exception {
        ConnectFuture future = client.connect(config.getProxyUser(), config.getProxyHost(), config.getProxyPort());
        session = future.verify(10, TimeUnit.SECONDS).getSession();
        log.info("SSH Tunnel Connection - session initialized");
    }

    private void initAuth() throws Exception {
        if (config.getAuthType() == KEY_PAIR) {
            initKeyPairAuth();
        } else {
            session.addPasswordIdentity(config.getProxyPassword());
        }

        session.auth().verify(10, TimeUnit.SECONDS);
        log.info("SSH Tunnel Connection - authentication succeeded");
    }

    private void initKeyPairAuth() throws Exception{
        String keyFile = config.getKeyFile();
        String keyPassphrase = Commons.nvl(config.getKeyPassphrase(), "");

        File privateKeyFile = new File(keyFile);
        try (InputStream keyFileStream = new FileInputStream(privateKeyFile)) {
            NamedResource namedResource = NamedResource.ofName(privateKeyFile.getName());
            FilePasswordProvider passwordProvider = (sessionContext, resourceKey, retryIndex) -> keyPassphrase;

            var keyPairs = SecurityUtils.loadKeyPairIdentities(session, namedResource, keyFileStream, passwordProvider);
            keyPairs.forEach(kp -> session.addPublicKeyIdentity(kp));
        }
    }

    private void initTracker() throws IOException {
        SshdSocketAddress localAddress = new SshdSocketAddress(localHost, localPort);
        SshdSocketAddress remoteAddress = new SshdSocketAddress(config.getRemoteHost(), config.getRemotePort());
        SshdSocketAddress boundAddress = session.startLocalPortForwarding(localPort, remoteAddress);

        tracker = new ExplicitPortForwardingTracker(session, true, localAddress, remoteAddress, boundAddress);
        log.info("SSH Tunnel Connection - tracker initialized");
    }

    public boolean isConnected() {
        return session != null && session.isAuthenticated();
    }

    public void disconnect() {
        try {
            if (tracker != null) {
                tracker.close();
                log.info("SSH Tunnel Connection - port forwarding stopped");
            }
            if (session != null && session.isOpen()) {
                session.close();
                log.info("SSH Tunnel Connection - session closed");
            }
            if (client != null && client.isOpen()) {
                client.stop();
                log.info("SSH Tunnel Connection - client stopped");
            }
        } catch (Exception e) {
            log.warn("Failed to close SSH tunnel connection", e);
            conditionallyLog(e);
        }
    }
}
