package e209880;

public abstract class Tire {

  protected double speed;
  protected double degradation;
  // my attributes
  protected TireType tireType;

  public TireType getTireType() {
    return tireType;
  }

  public double getSpeed() {
    return speed;
  }

  public double getDegradation() {
    return degradation;
  }

  abstract public void tick(TrackFeature f);

  public void degradeTire(double featureMultiplier, double featureRoughness, double tireMultiplier) {
      degradation += (featureMultiplier * featureRoughness * tireMultiplier);
  }

  public void speedControl() {
      if (speed >= 100) {
        speed -= (Math.min(75,degradation) * 0.25);
      }
  }
  
}
