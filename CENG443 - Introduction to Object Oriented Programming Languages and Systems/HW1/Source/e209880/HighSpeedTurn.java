package e209880;


public class HighSpeedTurn extends TrackFeature {

  public HighSpeedTurn(int turnNo, TurnDirection direction, double distance, double roughness) {
    super(turnNo,direction,distance,roughness);
    featureType = FeatureType.HIGH_SPEED_TURN;
  }
}
