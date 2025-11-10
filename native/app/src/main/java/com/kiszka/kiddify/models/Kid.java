    package com.kiszka.kiddify.models;

    import java.util.List;

    public class Kid {
        private int id;
        private String name;
        private String birthDate;
        private List<Integer> parents;

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public List<Integer> getParents() {
            return parents;
        }
        public void setParents(List<Integer> parents) {
            this.parents = parents;
        }
        public String getBirthDate() {
            return birthDate;
        }
        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }
    }
