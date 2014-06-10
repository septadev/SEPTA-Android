package org.septa.android.app.models.adapterhelpers;

/**
 * Created by bmayo on 6/10/14.
 */
public class TextSubTextImageModel extends TextImageModel {

    private String subText;

    public TextSubTextImageModel(String mainText, String subText, String imageNameBase, String imageNameSuffix) {
        super(mainText, imageNameBase, imageNameSuffix);
        this.subText = subText;
    }

    public String getSubText() {
        return subText;
    }

}
