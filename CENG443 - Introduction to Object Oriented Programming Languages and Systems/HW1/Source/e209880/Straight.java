package e209880;

public class Straight extends TrackFeature {

  public Straight(int turnNo, TurnDirection direction, double distance, double roughness) {
    super(turnNo,direction,distance,roughness);
    featureType = FeatureType.STRAIGHT;
  }
}
