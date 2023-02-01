package com.oracle.tac.pushData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
@Table(name="EMP")
public class Employee {
    @Id
    @Column(name="EMPNO")
    private Long empNo = null;
    @Column(name="ENAME")
    private String eName = null;
    @Column(name="JOB")
    private String job = null;
    @Column(name="MGR")
    private String mgrNo = null;
    @Column(name="HIREDATE")
    private Date hireDate = null;
    @Column(name="SAL")
    private Float salary = null;
    @Column(name="COMM")
    private Float commission = null;
    @Column(name="DEPTNO")
    private Long departmentNo = null;

}

