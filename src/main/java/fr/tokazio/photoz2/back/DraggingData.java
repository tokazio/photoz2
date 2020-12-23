package fr.tokazio.photoz2.back;

import java.awt.*;

public class DraggingData {

    private final Component source;
    private final Point sourcePoint;
    private final Component target;
    private final Point targetPoint;

    public DraggingData(Component source, Point sourcePoint, Component target, Point targetPoint) {
        this.source = source;
        this.sourcePoint = sourcePoint;
        this.target = target;
        this.targetPoint = targetPoint;
    }

    public Component getSource() {
        return source;
    }

    public Point getSourcePoint() {
        return sourcePoint;
    }

    public Component getTarget() {
        return target;
    }

    public Point getTargetPoint() {
        return targetPoint;
    }
}