package acme.features.api.anonymous.shout;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import acme.entities.shouts.Shout;
import acme.framework.entities.Anonymous;
import acme.framework.services.AbstractRestService;

@Service
public class ApiAnonymousShoutService implements AbstractRestService<Anonymous, Shout>{
	
	private final ApiAnonymousShoutRepository repository;

	@Autowired
	public ApiAnonymousShoutService(final ApiAnonymousShoutRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<Shout> getAll() {
		return this.repository.findAllShouts();
	}
	
	@Override
	public void save(final Shout shout) {
		shout.setMoment(new Date());
		this.repository.save(shout);
	}

	@Override
	public boolean authorise(final Authentication authentication) {
		return true;
	}

	@Override
	public void validate(final Shout entity, final List<FieldError> errors) {
		// Not external validation in this case
	}

}