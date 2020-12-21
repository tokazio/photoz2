package fr.tokazio.photoz2.front;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public interface DropListener {

    boolean dropped(MouseEvent e, Point dropPoint, List<File> selection);
}
