package e209880;

public class Car implements Comparable<Car> {

  private int carNo;
  private String driverName;
  private double totalTime;
  private Tire tire;

  public Car() {
    totalTime = 0;
  }

  public Car(String driverName, int carNo, Tire tire) {
    this.driverName = driverName;
    this.carNo = carNo;
    this.tire = tire;
    totalTime = 0;
  }

  @Override
  public int compareTo(Car car) {
    double compareValue = this.totalTime - car.getTotalTime();
    if (compareValue > 0.00001) return 1;
    if (compareValue < -0.00001) return -1;
    return 0;
  }


  public Tire getTire() {
    return tire;
  }

  public void setTire(Tire tire) {
    this.tire = tire;
  }

  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  public int getCarNo() {
    return carNo;
  }

  public void setCarNo(int carNo) {
    this.carNo = carNo;
  }

  public double getTotalTime() {
    return totalTime;
  }


  public void tick(TrackFeature feature) {
    double timeSpent = feature.getDistance() / tire.getSpeed() + Math.random();
    totalTime += timeSpent;
    tire.tick(feature);
    if (tire.getDegradation() > 70) {
      if (tire.getTireType() == TireType.SOFT_TIRE) {
        Tire newTire = new MediumTire();
        this.tire = newTire;
      } else if (tire.getTireType() == TireType.MEDIUM_TIRE || tire.getTireType() == TireType.HARD_TIRE) {
        Tire newTire = new SoftTire();
        this.tire = newTire;
      }
      totalTime += 25.0;
    }

  }

}
