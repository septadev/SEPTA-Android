/*
 * KML.java
 * Last modified on 04-06-2014 11:02-0400 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.models;

public class KMLModel {
    private Document document;

    static class Document {
        private String name;
        private Style style;
        private Folder folder;
        private Placemark[] placemarks;

        void Document(String name, Style style, Folder folder, Placemark[] placemarks) {
            this.name = name;
            this.style = style;
            this.folder = folder;
            this.placemarks = placemarks;
        }

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


        static class Style {
            private LabelStyle labelStyle;
            private LineStyle lineStyle;

            void Style(LabelStyle labelStyle, LineStyle lineStyle) {
                this.labelStyle = labelStyle;
                this.lineStyle = lineStyle;
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


            static class LabelStyle {
                private String color;
                private String style;

                void LabelStyle(String color, String style) {
                    this.color = color;
                    this.style = style;
                }

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

            static class LineStyle {
                private String color;
                private String style;

                void LineStyle(String color, String style) {
                    this.color = color;
                    this.style = style;
                }

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

        static class Folder {
            private String name;
            private boolean open;
            private Snippet snippet;
            private Placemark[] placemarks;

            void Folder(String name, boolean open, Snippet snippet, Placemark[] placemarks) {
                this.name = name;
                this.open = open;
                this.snippet = snippet;
                this.placemarks = placemarks;
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

        static class Snippet {
            private int maxLines;
            private String snippet;

            void Snipper(int maxLines, String snippet) {
                this.maxLines = maxLines;
                this.snippet = snippet;
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

        static class Placemark {
            private String name;
            private Snippet snippet;
            private String styleUrl;
            private MultiGeometry multiGeometry;

            void Placemark(String name, Snippet snippet, String styleUrl, MultiGeometry multiGeometry) {
                this.name = name;
                this.snippet = snippet;
                this.styleUrl = styleUrl;
                this.multiGeometry = multiGeometry;
            }

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

        static class MultiGeometry {
            private LineString lineString;

            static class LineString {
                private int tessellate;
                private Coordinate[] coordinates;

                void LineString(int tessellate, Coordinate[] coordinates) {
                    this.tessellate = tessellate;
                    this.coordinates = coordinates;
                }

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


                static class Coordinate {
                    private double latitude;
                    private double longitude;
                    private int notsure;

                    void Coordinate(double latitude, double longitude, int notsure) {
                        this.latitude = latitude;
                        this.longitude = longitude;
                        this.notsure = notsure;
                    }

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
