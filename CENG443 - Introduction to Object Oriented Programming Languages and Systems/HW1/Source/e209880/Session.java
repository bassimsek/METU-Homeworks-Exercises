package e209880;
import java.util.*;

public class Session {

  private Track track;
  private ArrayList<Team> teamList;
  private int totalLaps;

  public Session() {
  }

  public Session(Track track, ArrayList<Team> teamList, int totalLaps) {
    this.track = track;
    this.teamList = teamList;
    this.totalLaps = totalLaps;
  }

  public Track getTrack() {
    return track;
  }

  public void setTrack(Track track) {
    this.track = track;
  }

  public ArrayList<Team> getTeamList() {
    return teamList;
  }

  public void setTeamList(ArrayList<Team> teamList) {
    this.teamList = teamList;
  }

  public int getTotalLaps() {
    return totalLaps;
  }

  public void setTotalLaps(int totalLaps) {
    this.totalLaps = totalLaps;
  }


  public void simulate() {
      if (!track.isValidTrack()) {
        System.out.println("Track is invalid.Simulation aborted!");
        return;
      } else {
        System.out.println("Track is valid.Strating simulation on " + track.getTrackName() + " for " + totalLaps + " laps.");
        int trackLength = track.getTrackLength();
        int lapCount = totalLaps;
        if (trackLength < 1) {
          return;
        } else {
            while(lapCount > 0) {
                simulateOneLap();
                lapCount--;
            }
          }
      }

      System.out.println(printWinnerTeam());
      System.out.println(printTimingTable());

  }

  private void simulateOneLap() {
    for(int h =0; h<track.getFeatureList().size();h++) {
        for(int i = 0;i<teamList.size();i++) {
            for(int j = 0;j<teamList.get(i).getCarList().size();j++) {
              teamList.get(i).getCarList().get(j).tick(track.getFeatureList().get(h));
            }
        }
    }

  }

  
  public String printWinnerTeam() {

      List<Car> allCars = getSortedCars();
      String resultString = "";
      String winnerTeam =  null;

      for(int m=0;m<teamList.size();m++) {
          for(int n=0;n<teamList.get(m).getCarList().size();n++) {
              if(teamList.get(m).getCarList().get(n).getCarNo() == allCars.get(0).getCarNo()) {
                  winnerTeam = teamList.get(m).getName();
              }
          }
      }

    for(int h=0;h<teamList.size();h++) {
        if(teamList.get(h).getName().equals(winnerTeam)) {
            resultString = resultString.concat("Team " + winnerTeam + " wins.");
            for(int k=0;k<teamList.get(h).getTeamColors().length;k++) {
                resultString = resultString.concat(teamList.get(h).getTeamColors()[k]);
                if ((k+2) == teamList.get(h).getTeamColors().length) {
                    resultString = resultString.concat(" and ");
                } else if ((k+1) == teamList.get(h).getTeamColors().length) {
                    resultString = resultString.concat(" flags are waving everywhere.");
                } else {
                    resultString = resultString.concat(", ");
                }
            }
            break;
        }
    }
    return resultString;

   }


  private String printTimingTable() {

      List<Car> allCars = getSortedCars();
      String resultString = "";

      int hours = 0;
      int minutes = 0;
      double currentTotalTime = 0.0;

      for(Car car: allCars) {
        resultString = resultString.concat(car.getDriverName() + "(" + car.getCarNo() + "): ");
        currentTotalTime = car.getTotalTime();
        hours = (int) (currentTotalTime / 3600.0);
        if (hours < 10) {
        resultString = resultString.concat("0" + hours + ":");
        } else {
        resultString = resultString.concat(hours + ":");
        }
        currentTotalTime = currentTotalTime - (3600.0*hours);

        minutes = (int) (currentTotalTime / 60.0);
        if (minutes < 10) {
        resultString = resultString.concat("0" + minutes + ":");
        } else {
        resultString = resultString.concat(minutes + ":");
        }
        currentTotalTime = currentTotalTime - (60.0*minutes);

        if(currentTotalTime < 10) {
        resultString = resultString.concat("0");
        }
        String x = String.format(Locale.US,"%.3f", currentTotalTime);
        resultString = resultString.concat(x);
        resultString = resultString.concat("\n");
      }

      return resultString;

  }


  private List<Car> getSortedCars() {
      List<Car> allCars = new ArrayList<>();

      for(int i=0;i<teamList.size();i++) {
          for(int j=0;j<teamList.get(i).getCarList().size();j++) {
              allCars.add(teamList.get(i).getCarList().get(j));
          }
      }

      Collections.sort(allCars);
      return allCars;
  }

}
