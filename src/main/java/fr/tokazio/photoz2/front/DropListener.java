package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.VirtualFolder;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public interface DropListener<T extends VirtualFolder> {
    T dropped(MouseEvent e, Point dropPoint, List<File> selection);
}
