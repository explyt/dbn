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

package com.dbn.database.common.metadata.impl;

import com.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dbn.database.common.metadata.def.DBJavaMethodMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBJavaMethodMetadataImpl extends DBObjectMetadataBase implements DBJavaMethodMetadata  {

	public DBJavaMethodMetadataImpl(ResultSet resultSet) {
		super(resultSet);
	}

	@Override
	public String getClassName() throws SQLException {
		return getString("CLASS_NAME");
	}

	@Override
	public String getMethodName() throws SQLException {
		return getString("METHOD_NAME");
	}

	@Override
	public short getPosition() throws SQLException {
		return resultSet.getShort("POSITION");
	}

	@Override
	public boolean isPublic() throws SQLException {
		return isYesFlag("IS_PUBLIC");
	}

	@Override
	public boolean isStatic() throws SQLException {
		return isYesFlag("IS_STATIC");
	}
}
