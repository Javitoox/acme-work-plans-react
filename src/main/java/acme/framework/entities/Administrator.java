/*
 * Administrator.java
 *
 * Copyright (C) 2012-2021 Rafael Corchuelo.
 *
 * In keeping with the traditional purpose of furthering education and research, it is
 * the policy of the copyright owner to permit non-commercial use and redistribution of
 * this software. It has been tested carefully, but it is not guaranteed for any particular
 * purposes. The copyright owner does not offer any warranties or representations, nor do
 * they accept any liabilities with respect to them.
 */

package acme.framework.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class Administrator extends UserRole {

	// Serialisation identifier -----------------------------------------------

	protected static final long serialVersionUID = 1L;

	// Attributes -------------------------------------------------------------

	// Derived attributes -----------------------------------------------------

	// Relationships ----------------------------------------------------------

}
