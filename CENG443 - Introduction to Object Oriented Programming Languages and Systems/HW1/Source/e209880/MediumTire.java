package e209880;

public class MediumTire extends Tire {

  public MediumTire() {
    this.speed = 310;
    this.degradation = 0;
    tireType = TireType.MEDIUM_TIRE;
  }

  
  public void tick(TrackFeature f) {
      degradeTire(f.getFeatureType().getMultiplier(), f.getRoughness() ,1.1);
      speedControl();
  }
}
