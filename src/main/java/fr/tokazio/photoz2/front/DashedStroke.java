package fr.tokazio.photoz2.front;

import java.awt.*;

public class DashedStroke extends BasicStroke {


    public DashedStroke(int width) {
        super(width,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                width / 2f,
                new float[]{width, 0f, width},
                width);
    }
}
