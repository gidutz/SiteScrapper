/**
 * Created by gidutz on 5/3/14.
 */
public class Store implements Comparable<Store> {
    private String title;

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    private String type;
    private String city;
    private String phone;
    private double lat;
    private double lng;
    private int hash = 0;

    /**
     * constructs a store using a builder
     *
     * @param storeBuilder
     */
    public Store(StoreBuilder storeBuilder) {
        this.title = storeBuilder.title;
        this.type = storeBuilder.type;
        this.city = storeBuilder.city;
        this.phone = storeBuilder.phone;
        this.lat = storeBuilder.lat;
        this.lng = storeBuilder.lng;
        this.hash = hashCode();
    }


    /**
     * Helper class the enables an elegant way to construct a new store
     */
    public static class StoreBuilder {
        private String title = "";
        private String type = "";
        private String city = "";
        private String phone = "";
        private double lat = 0L;
        private double lng = 0L;

        public StoreBuilder() {

        }

        public StoreBuilder title(String title) {
            this.title = title;
            return this;

        }

        /**
         * Sets the Type parameter of the built Store
         *
         * @param type
         * @return
         */
        public StoreBuilder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * sets the city parameter of the built Store
         *
         * @param city
         * @return
         */
        public StoreBuilder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * sets the phone number of the store
         */
        public StoreBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        /**
         * sets the latitude coordinate of the store
         */
        public StoreBuilder lat(double lat) {
            this.lat = lat;
            return this;

        }

        /**
         * sets the latitude using a string as input
         *
         * @param lat
         * @return
         */
        public StoreBuilder lat(String lat) {
            try {
                lat(Double.parseDouble(lat));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                System.out.println(lat);
                System.exit(24);
            }
            return this;
        }

        /**
         * sets the latitude using a string as input
         *
         * @param lng
         * @return
         */
        public StoreBuilder lng(String lng) {
            try {
                lng(Double.parseDouble(lng));
            } catch (NumberFormatException ex) {

            }
            return this;
        }

        /*
            sets the Long coordinate of the store
         */
        public StoreBuilder lng(Double lng) {
            this.lng = lng;
            return this;
        }

        public Store build() {
            return new Store(this);
        }
    }

    /**
     * compares two store hashes, if same then the stores are considered equal
     *
     * @param o The other store to compare with
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Store)) {
            return false;
        }
        if (o == null) {
            return false;
        }
        Store other = (Store) o;
        return hashCode() == other.hashCode();
    }


    /**
     * stores are compared relatively to their hash
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Store o) {

        return hashCode() - o.hashCode();
    }

    /*
    * returns a hash of the store
    * the hash is driven from the store location coordinates and the store name
    * two stores with the same name in the same position shall have an equal hash
     */
    @Override
    public int hashCode() {
        if (hash == 0) {
            int base = 0;
            for (int i = 0; i < title.length(); i++) {
                base += title.charAt(i);
            }
            for (int i = 0; i < city.length(); i++) {
                base += city.charAt(i);
            }
            hash = base + ((int) (lat * 1000.0) + (int) (lng * 1000.0));
        }

        return hash;
    }

    /**
     * creates a string
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toRow(this.hash));
        sb.append(toRow(this.title));
        sb.append(toRow(this.type));
        sb.append(toRow(this.city));
        sb.append(toRow(this.phone));
        sb.append(toRow(this.lat));
        sb.append(toRow(this.lng));

        return sb.toString();
    }

    private String toRow(Object value) {
        return value.toString() + "\t";
    }
}
