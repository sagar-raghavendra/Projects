import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Day14 {

    Map<String, Element> reactionMap = new HashMap<>();
    long oreQuantityRequired;

    public static void main(String[] args) throws IOException {
        Day14 day14 = new Day14();

        //Create map of all reactions
        Files.lines(Paths.get("/Users/sagar/Development/AdventOfCode/input3.txt")).
                map(x -> x.split("=>")).collect(Collectors.toMap(x -> x[1], x -> x[0])).forEach(day14::createElement);

        System.out.println("ORE quantity required  = " + day14.getOreQuantityForFuel( 1));
        System.out.println("Total FUEL produced for 1 Trillion ORE = " + day14.calculateFuelProducedForGivenOre(1000000000000L));
    }

    private long getOreQuantityForFuel(long fuelQuantity){
        reactionMap.forEach((k, v) -> {v.setExcessQuantityProduced(0); v.setTotalQuantityRequired(0);});
        calculateOreCount(reactionMap.get("FUEL"), fuelQuantity);
        long ore = oreQuantityRequired;
        oreQuantityRequired = 0;
        return ore;
    }

    private void createElement(String output, String inputs) {
        String[] outputValues = output.trim().split("\\s");
        Element element = getElement(outputValues[1]);
        element.setMinimumQuantityProduced(Integer.parseInt(outputValues[0]));
        if (inputs.contains("ORE")) {
            element.setDerivedFromOre(true);
            element.setInputQuantityRequired(Integer.parseInt(inputs.trim().split("\\s")[0]));
        }
        element.setInputElements(Arrays.stream(inputs.split(",")).filter(x -> !x.trim().contains("ORE")).
                map(x -> x.trim().split("\\s")).collect(Collectors.toMap(x -> (getElement(x[1])), x -> Integer.valueOf(x[0]), (a, b) -> b,LinkedHashMap::new)));
        reactionMap.put(outputValues[1], element);
    }

    private Element getElement(String elementName) {
        if (reactionMap.get(elementName) == null) {
            Element element = new Element();
            element.setElementName(elementName);
            reactionMap.put(elementName, element);
        }
        return reactionMap.get(elementName);
    }

    private void calculateOreCount(Element outputElement, long outputElementQuantity) {
        if (outputElement.isDerivedFromOre()) {
            long reactionRequiredCount = getMinimumReactionsNeeded(outputElement, outputElementQuantity, outputElement.minimumQuantityProduced);
            oreQuantityRequired += outputElement.getInputQuantityRequired()*reactionRequiredCount;
            long totalRequiredCount = reactionRequiredCount * outputElement.getMinimumQuantityProduced();
            totalRequiredCount += outputElement.getTotalQuantityRequired();
            outputElement.setTotalQuantityRequired(totalRequiredCount);
        } else {
            Map<Element, Integer> inputElements = outputElement.getInputElements();
            inputElements.forEach((inputElement, inputElementQuantity) -> {
                long finalQuantity;
                if (!inputElement.isDerivedFromOre()) {
                    finalQuantity = getMinimumReactionsNeeded(inputElement, outputElementQuantity * inputElementQuantity, inputElement.getMinimumQuantityProduced());
                } else {
                    finalQuantity = inputElementQuantity * outputElementQuantity;
                }
                calculateOreCount(inputElement, finalQuantity);
            });
        }
    }

    private long getMinimumReactionsNeeded(Element element, long elementRequiredCount, long minimumElementsProduced) {
        long netQuantityRequired = elementRequiredCount - element.getExcessQuantityProduced();
        if (netQuantityRequired > 0) {
            element.setExcessQuantityProduced(0);
            if (elementRequiredCount >= minimumElementsProduced) {
                long reactionCount = netQuantityRequired / minimumElementsProduced;
                if (netQuantityRequired % minimumElementsProduced > 0) {
                    ++reactionCount;
                    element.setExcessQuantityProduced((minimumElementsProduced * reactionCount) - netQuantityRequired);
                }
                return reactionCount;
            } else
                element.setExcessQuantityProduced(minimumElementsProduced - netQuantityRequired);
            return 1;
        } else {
            if (netQuantityRequired == 0) {
                element.setExcessQuantityProduced(0);
            } else {
                element.setExcessQuantityProduced(element.getExcessQuantityProduced() - elementRequiredCount);
            }
            return 0;
        }
    }

    private long calculateFuelProducedForGivenOre(long totalOre) {
        long first = 0, middle = -1;
        long last = totalOre;
        while(first <= last) {
            middle = (first+last)/2;
            long midVal = getOreQuantityForFuel(middle);
            if(midVal < totalOre) {
                first = middle+1;
            } else if (midVal > totalOre){
                last = middle-1;
            } else {
                return middle;
            }
        }
        return middle-1;
    }
}

@Getter
@Setter
class Element{
    String elementName;
    long minimumQuantityProduced;
    //Populated only if derived from ORE
    long inputQuantityRequired;
    //Populated only if derived from ORE
    long totalQuantityRequired;
    long excessQuantityProduced;
    boolean isDerivedFromOre;
    Map<Element, Integer> inputElements;
}