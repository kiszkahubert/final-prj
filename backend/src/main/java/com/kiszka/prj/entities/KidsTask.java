package com.kiszka.prj.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "kids_tasks")
@IdClass(KidsTask.KidsTaskId.class)
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class KidsTask {
    @Id
    @Column(name = "task_id")
    private Integer taskId;
    @Id
    @Column(name = "kid_id")
    private Integer kidId;
    @ManyToOne
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    @JsonBackReference
    private Task task;
    @NoArgsConstructor @AllArgsConstructor @Getter @Setter
    public static class KidsTaskId implements Serializable{
        private Integer taskId;
        private Integer kidId;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            KidsTaskId that = (KidsTaskId) o;
            return Objects.equals(taskId, that.taskId) && Objects.equals(kidId, that.kidId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(taskId, kidId);
        }
    }
}
