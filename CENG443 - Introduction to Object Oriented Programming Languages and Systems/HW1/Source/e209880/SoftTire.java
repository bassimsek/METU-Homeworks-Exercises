package e209880;


public class SoftTire extends Tire {

  public SoftTire() {
    this.speed = 350;
    this.degradation = 0;
      tireType = TireType.SOFT_TIRE;
  }

  
  public void tick(TrackFeature f) {
      degradeTire(f.getFeatureType().getMultiplier(), f.getRoughness() ,1.2);
      speedControl();
  }

}
