package com.oracle.tac.pushData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 *
 * @author MPFEIFER
 */
@Getter
@Setter
@Entity
@Table(name="DEPT")
public class Department {
    @Id
    @Column(name="DEPTNO")
    private Long departmentNo = null;
    @Column(name="DNAME")
    private String departmentName = null;
    @Column(name="LOC")
    private String location = null;
}

