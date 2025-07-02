package com.kiszka.prj.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "parents_kids")
@IdClass(ParentKid.ParentKidId.class)
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class ParentKid {
    @Id
    @Column(name = "parent_id")
    private Integer parentId;
    @Id
    @Column(name = "kid_id")
    private Integer kidId;
    public static class ParentKidId implements Serializable {
        private Integer parentId;
        private Integer kidId;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            ParentKidId that = (ParentKidId) o;
            return Objects.equals(parentId, that.parentId) && Objects.equals(kidId, that.kidId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentId, kidId);
        }
    }
}
