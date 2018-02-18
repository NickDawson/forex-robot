package com.trading.forex.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by hsouidi on 21/01/18.
 */
@Entity
@Table(name="app_role")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Role {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="role_name")
    private String roleName;

    @Column(name="description")
    private String description;


}