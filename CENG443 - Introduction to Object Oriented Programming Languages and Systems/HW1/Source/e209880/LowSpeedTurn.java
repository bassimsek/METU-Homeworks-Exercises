package e209880;


public class LowSpeedTurn extends TrackFeature {

  public LowSpeedTurn(int turnNo, TurnDirection direction, double distance, double roughness) {
    super(turnNo,direction,distance,roughness);
    featureType = FeatureType.LOW_SPEED_TURN;
  }
}
