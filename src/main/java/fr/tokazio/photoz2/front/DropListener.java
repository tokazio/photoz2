package fr.tokazio.photoz2.front;

import fr.tokazio.photoz2.back.PictLoaderList;
import fr.tokazio.photoz2.back.VirtualFolder;

import java.awt.*;

public interface DropListener<T extends VirtualFolder> {

    void drop(PictLoaderList selection);

    T dropTo(Point dropPoint);

    void dropped();
}
