package com.dbn.oracleAI;

import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class ProfileListService {
  private final Map<ConnectionId, List<Profile>> profileList;
  private final PropertyChangeSupport support;
  private ConnectionId currConnection;

  public ProfileListService() {
    profileList = new HashMap<>();
    support = new PropertyChangeSupport(this);
  }

  public static ProfileListService getInstance() {
    return ServiceManager.getService(ProfileListService.class);
  }

  public List<Profile> getProfileList() {
    return new ArrayList<>(profileList.get(currConnection));
  }

  public void addProfile(Profile profile) {
    profileList.computeIfAbsent(currConnection, k -> new ArrayList<>());
    profileList.get(currConnection).add(profile);
  }

  public void addProfiles(List<Profile> profiles) {
    profileList.computeIfAbsent(currConnection, k -> new ArrayList<>());
    profileList.get(currConnection).addAll(profiles);
  }

  public void setCurrConnection(ConnectionId connection) {
    currConnection = connection;
  }

  public ConnectionId getCurrConnection() {
    return currConnection;
  }

  public void fireUpdatedProfileListEvent() {
    support.firePropertyChange("profileList", null, profileList);
  }

  public void removeProfile(Profile profile) {
    profileList.get(currConnection).remove(profile);
  }

  public void clearProfiles() {
    profileList.remove(currConnection);
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    support.addPropertyChangeListener(pcl);
  }
}
