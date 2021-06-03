package group;

import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.Slider;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestController {
    public FlowPane flowPane;
    public Slider fixedSlider;
    List<Slider> sliders = new ArrayList<>();
    Integer number = 250;
    Integer current = 0;

    javafx.event.EventHandler<MouseEvent> sliderEventHandler = event -> {
        Slider thisSlider = (Slider)event.getSource();
        int sum = 0;
        if(sliders.size()>1) {
            sum = sliders.stream().map(slider -> (int) slider.getValue()).reduce(0, (integer, integer2) -> integer + integer2);
            int difference = number - sum;
            int adjustment = difference / (sliders.size() - 1);
//            int remainder = difference - adjustment * (sliders.size()-1);
//            System.out.println(" difference: " +difference + " adjustment: " + adjustment);
            sliders.stream().filter(slider -> !slider.equals(thisSlider)).forEach(slider -> slider.setValue(slider.getValue() + adjustment));
            sum = sliders.stream().map(slider -> (int) slider.getValue()).reduce(0, (integer, integer2) -> integer + integer2);
            int remainder = number - sum;
//            System.out.println("remainder: "+remainder);
            for(int i=0;remainder>0;i++){
                i%=sliders.size();
                if(!sliders.get(i).equals(thisSlider) && sliders.get(i).getValue()<number)
                    sliders.get(i).setValue((int)sliders.get(i).getValue()+1);
                remainder--;
            }
            for(int i=0;remainder<0;i++){
                i%=sliders.size();
                if(!sliders.get(i).equals(thisSlider) && sliders.get(i).getValue()>0)
                    sliders.get(i).setValue((int)sliders.get(i).getValue()-1);
                remainder++;
            }
        }
        //        sliders.stream().filter(slider -> !slider.equals(thisSlider)).forEach(slider -> );
//        int newSum = sliders.stream().map(slider -> (int) slider.getValue()).reduce(0, (integer, integer2) -> integer + integer2);
//        System.out.println("sum: " + sum + " new sum: " + newSum);
    };

    public void addSlider(ActionEvent actionEvent) {
        Slider slider = new Slider();
        slider.setMax((double)number);
        slider.setSnapToTicks(false);
        slider.setBlockIncrement(1);
        slider.setOnMouseDragged(sliderEventHandler);
        slider.setOnMouseClicked(sliderEventHandler);

        sliders.add(slider);
        flowPane.getChildren().add(slider);
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        System.out.println(fixedSlider.getValue());
    }
}
