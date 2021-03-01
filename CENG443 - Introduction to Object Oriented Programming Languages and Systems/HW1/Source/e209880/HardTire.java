package e209880;


public class HardTire extends Tire {

  public HardTire() {
    this.speed = 275;
    this.degradation = 0;
    tireType = TireType.HARD_TIRE;
  }


  public void tick(TrackFeature f) {
      degradeTire(f.getFeatureType().getMultiplier(), f.getRoughness() ,1.0);
      speedControl();
  }
}
