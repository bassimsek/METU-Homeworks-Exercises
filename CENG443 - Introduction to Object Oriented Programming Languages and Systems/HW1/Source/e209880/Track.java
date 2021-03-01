package e209880;

import java.util.ArrayList;

public class Track {

  private String trackName;
  private ArrayList<TrackFeature> featureList;
  private boolean isClockwise;
  // my attributes
  private int featureCall = -1;

  public Track() {
  }

  public Track(String trackName, ArrayList<TrackFeature> featureList, boolean isClockwise) {
      this.trackName = trackName;
      this.featureList = featureList;
      this.isClockwise = isClockwise;
  }

  public String getTrackName() {
    return trackName;
  }

  public void setTrackName(String trackName) { this.trackName = trackName; }

  public ArrayList<TrackFeature> getFeatureList() {
    return featureList;
  }

  public void setFeatureList(ArrayList<TrackFeature> featureList) {
    this.featureList = featureList;
  }

  public boolean isClockwise() {
    return isClockwise;
  }

  public void setClockwise(boolean clockwise) {
    isClockwise = clockwise;
  }

  
  public int getTrackLength() {
    return featureList.size();
  }

  
  public TrackFeature getNextFeature() {
    if (featureList.size() == 0) {
        return null;
    }
    featureCall++;
    if (featureCall >= featureList.size()) {
        featureCall = 0;
    }

    return featureList.get(featureCall);
  }


  public void addFeature(TrackFeature feature) {
      featureList.add(feature);
  }


   
  public boolean isValidTrack() {
      int leftTurnCount = 0;
      int rightTurnCount = 0;
      boolean validation = false;

      if(featureList.size() == 0) {
          return false;
      }

      if (featureList.get(0).getTurnDirection() == TurnDirection.STRAIGHT && featureList.get(featureList.size()-1).getTurnDirection() == TurnDirection.STRAIGHT) {
          for(int i=0;i<featureList.size();i++) {
              if (featureList.get(i).getTurnDirection() == TurnDirection.LEFT) {
                  leftTurnCount++;
              }
              else if (featureList.get(i).getTurnDirection() == TurnDirection.RIGHT) {
                  rightTurnCount++;
              }
          }
          if (isClockwise) {
              if (rightTurnCount == (leftTurnCount+4)) {
                  validation = true;
              } else {
                  validation = false;
              }
          }
          else if (!isClockwise) {
              if (leftTurnCount == (rightTurnCount+4)) {
                  validation =  true;
              } else {
                  validation =  false;
              }
          }
      }
      return validation;
  }
}
