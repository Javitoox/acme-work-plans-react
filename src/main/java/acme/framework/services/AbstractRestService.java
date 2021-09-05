package acme.framework.services;

import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import acme.framework.entities.UserRole;

@Service
@Transactional(TxType.MANDATORY)
public interface AbstractRestService<R extends UserRole, E>{

	List<E> getAll();
	
	void save(final E entity);
	
	boolean authorise(final Authentication authentication);
	
	void validate(final E entity, List<FieldError> errors);

}
