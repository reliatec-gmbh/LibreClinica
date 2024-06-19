/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright (C) 2003 - 2011 Akaza Research
 * copyright (C) 2003 - 2019 OpenClinica
 * copyright (C) 2020 - 2024 LibreClinica
 */
package org.akaza.openclinica.web.pform.manifest;

import java.util.ArrayList;

public class Manifest {
    private ArrayList<MediaFile> mediaFile = null;

    public Manifest() {
        mediaFile = new ArrayList<MediaFile>();
    }

    public void add(MediaFile mediaFile) {
        this.mediaFile.add(mediaFile);
    }

    public ArrayList<MediaFile> getMediaFile() {
        return mediaFile;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFile) {
        this.mediaFile = mediaFile;
    }
}
