/*
 * KML.java
 * Last modified on 04-06-2014 11:02-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

import com.google.android.gms.internal.id;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class KMLModel {
    @Element(name="Document")
    private Document document;

    @Root
    static class Document {
        @Element
        private String name;
        @Element
        private Style style;
        @Element(name="Folder")
        private Folder folder;
        @ElementList(required=false)
        private Placemark[] placemarks;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Style getStyle() {
            return style;
        }

        public void setStyle(Style style) {
            this.style = style;
        }

        public Folder getFolder() {
            return folder;
        }

        public void setFolder(Folder folder) {
            this.folder = folder;
        }

        public Placemark[] getPlacemarks() {
            return placemarks;
        }

        public void setPlacemarks(Placemark[] placemarks) {
            this.placemarks = placemarks;
        }

        @Root
        static class Style {
            @Attribute
            private String id;

            @Element
            private LabelStyle labelStyle;
            @Element
            private LineStyle lineStyle;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public LabelStyle getLabelStyle() {
                return labelStyle;
            }

            public void setLabelStyle(LabelStyle labelStyle) {
                this.labelStyle = labelStyle;
            }

            public LineStyle getLineStyle() {
                return lineStyle;
            }

            public void setLineStyle(LineStyle lineStyle) {
                this.lineStyle = lineStyle;
            }

            @Root
            static class LabelStyle {
                @Element
                private String color;
                @Element
                private String style;

                public String getColor() {
                    return color;
                }

                public void setColor(String color) {
                    this.color = color;
                }

                public String getStyle() {
                    return style;
                }

                public void setStyle(String style) {
                    this.style = style;
                }
            }

            @Root
            static class LineStyle {
                @Element
                private String color;
                @Element
                private String style;

                public String getColor() {
                    return color;
                }

                public void setColor(String color) {
                    this.color = color;
                }

                public String getStyle() {
                    return style;
                }

                public void setStyle(String style) {
                    this.style = style;
                }
            }
        }

        @Root
        static class Folder {
            @Attribute
            private String id;

            @Element
            private String name;
            @Element
            private boolean open;
            @Element
            private Snippet snippet;
            @Element
            private Placemark[] placemarks;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public boolean getOpen() {
                return open;
            }

            public void setOpen(boolean open) {
                this.open = open;
            }

            public Snippet getSnippet() {
                return snippet;
            }

            public void setSnippet(Snippet snippet) {
                this.snippet = snippet;
            }

            public Placemark[] getPlacemarks() {
                return placemarks;
            }

            public void setPlacemarks(Placemark[] placemarks) {
                this.placemarks = placemarks;
            }
        }

        @Root
        static class Snippet {
            @Attribute
            private String id;

            @Element
            private int maxLines;
            @Element
            private String snippet;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public int getMaxLines() {
                return maxLines;
            }

            public void setMaxLines(int maxLines) {
                this.maxLines = maxLines;
            }

            public String getSnippet() {
                return snippet;
            }

            public void setSnippet(String snippet) {
                this.snippet = snippet;
            }
        }

        @Root
        static class Placemark {
            @Element
            private String name;
            @Element
            private Snippet snippet;
            @Element
            private String styleUrl;
            @Element
            private MultiGeometry multiGeometry;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Snippet getSnippet() {
                return snippet;
            }

            public void setSnippet(Snippet snippet) {
                this.snippet = snippet;
            }

            public String getStyleUrl() {
                return styleUrl;
            }

            public void setStyleUrl(String styleUrl) {
                this.styleUrl = styleUrl;
            }

            public MultiGeometry getMultiGeometry() {
                return multiGeometry;
            }

            public void setMultiGeometry(MultiGeometry multiGeometry) {
                this.multiGeometry = multiGeometry;
            }
        }

        @Root
        static class MultiGeometry {
            @Element
            private LineString lineString;

            @Root
            static class LineString {
                @Element
                private int tessellate;
                @Element
                private Coordinate[] coordinates;

                public int getTessellate() {
                    return tessellate;
                }

                public void setTessellate(int tessellate) {
                    this.tessellate = tessellate;
                }

                public Coordinate[] getCoordinates() {
                    return coordinates;
                }

                public void setCoordinates(Coordinate[] coordinates) {
                    this.coordinates = coordinates;
                }

                @Root
                static class Coordinate {
                    @Element
                    private double latitude;
                    @Element
                    private double longitude;
                    @Element
                    private int notsure;

                    public double getLatitude() {
                        return latitude;
                    }

                    public void setLatitude(double latitude) {
                        this.latitude = latitude;
                    }

                    public double getLongitude() {
                        return longitude;
                    }

                    public void setLongitude(double longitude) {
                        this.longitude = longitude;
                    }

                    public int getNotsure() {
                        return notsure;
                    }

                    public void setNotsure(int notsure) {
                        this.notsure = notsure;
                    }
                }
            }
        }

    }
}
