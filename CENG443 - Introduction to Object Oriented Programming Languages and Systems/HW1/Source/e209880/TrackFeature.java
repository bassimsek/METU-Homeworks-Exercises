package e209880;

public abstract class TrackFeature {

  protected int featureNo;
  protected TurnDirection turnDirection;
  protected double distance;
  protected double roughness;
  // my attributes
  protected FeatureType featureType;

  public TrackFeature(int featureNo, TurnDirection turnDirection, double distance, double roughness) {
    this.featureNo = featureNo;
    this.turnDirection = turnDirection;
    this.distance = distance;
    this.roughness = roughness;
  }

  public FeatureType getFeatureType() {
    return featureType;
  }

  public int getFeatureNo() {
    return featureNo;
  }

  public double getRoughness() {
    return roughness;
  }

  public double getDistance() {
    return distance;
  }

  public TurnDirection getTurnDirection() {
    return turnDirection;
  }

}
