//package com.orcunbassimsek;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PartsStore {

    private List<Part> data;

    public PartsStore() {
        data = new ArrayList<>();
        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader("pcparts.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] newLine = line.split(",");
                Part newPart = createPart(newLine);
                data.add(newPart);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Part createPart(String[] newLine) {

        Part newPart = null;

        switch(newLine[0]) {
            case "Hard Drive":
                newPart = new HardDrive(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4]);
                break;
            case "Monitor":
                newPart = new Monitor(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5]);
                break;
            case "PSU":
                newPart = new PSU(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5]);
                break;
            case "Motherboard":
                newPart = new Motherboard(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5]);
                break;
            case "CPU":
                newPart = new CPU(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5]);
                break;
            case "GPU":
                newPart = new GPU(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5], newLine[6]);
                break;
            case "Mouse":
                newPart = new Mouse(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4]);
                break;
            case "Memory":
                newPart = new Memory(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4], newLine[5], newLine[6]);
                break;
            case "Keyboard":
                newPart = new Keyboard(newLine[0], newLine[1], newLine[2], newLine[3], newLine[4]);
                break;
        }

        return newPart;
    }


    public void FindPartsWithBrand(String type, String brand) {

        List<Part> result;

        if (type != null) {
            result = data.stream()
                    .filter(part -> part.getType().equals(type) && part.getBrand().equals(brand))
                    .collect(Collectors.toList());
        } else {
            result = data.stream()
                    .filter(part -> part.getBrand().equals(brand))
                    .collect(Collectors.toList());
        }

        for (Part part : result) {
            System.out.println(part);
        }
    }


    public void TotalPrice(String type, String brand, String model) {

        double sum = 0;

        if(type == null) {
            if (brand == null && model != null) {
                sum = data.stream()
                        .filter(part -> part.getModel().equals(model))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            } else if (brand != null && model == null) {
                sum = data.stream()
                        .filter(part -> part.getBrand().equals(brand))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            } else if (brand != null && model != null) {
                sum = data.stream()
                        .filter(part -> part.getBrand().equals(brand))
                        .filter(part -> part.getModel().equals(model))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            }
        } else {
            if (brand == null && model == null) {
                sum = data.stream()
                        .filter(part -> part.getType().equals(type))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            } else if (brand != null && model == null) {
                sum = data.stream()
                        .filter(part -> part.getType().equals(type))
                        .filter(part -> part.getBrand().equals(brand))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            } else if (brand == null && model != null) {
                sum = data.stream()
                        .filter(part -> part.getType().equals(type))
                        .filter(part -> part.getModel().equals(model))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            } else if (brand != null && model != null) {
                sum = data.stream()
                        .filter(part -> part.getType().equals(type))
                        .filter(part -> part.getBrand().equals(brand))
                        .filter(part -> part.getModel().equals(model))
                        .mapToDouble(Part::getPriceAsDouble)
                        .sum();
            }
        }

        System.out.printf(Locale.US,"%.2f USD\n",sum);
    }



    public void UpdateStock() {

        long numberOfPartsWithZeroPrice = data.stream().filter(part -> part.getPriceAsDouble() == 0).count();

        data = data.stream()
                .filter(part -> part.getPriceAsDouble() > 0)
                .collect(Collectors.toList());

        System.out.println(numberOfPartsWithZeroPrice + " items removed.");
    }



    public void FindCheapestMemory(int capacity) {

        Part cheapestMemory = data.stream()
                .filter(part -> part instanceof Memory)
                .map(part -> (Memory) part)
                .filter(memory -> Integer.parseInt(memory.getCapacity().substring(0, memory.getCapacity().length()-2)) >= capacity)
                .min(Comparator.comparing(Part::getPriceAsDouble))
                .get();

        System.out.println(cheapestMemory);
    }



    public void FindFastestCPU() {

        Part fastestCPU = data.stream()
                .filter(part -> part instanceof CPU)
                .map(part -> (CPU) part)
                .max(Comparator.comparing(cpu -> Double.parseDouble(cpu.getCoreCount()) * Double.parseDouble(cpu.getClockSpeed().substring(0, cpu.getClockSpeed().length()-3))))
                .get();

        System.out.println(fastestCPU);

    }


}
