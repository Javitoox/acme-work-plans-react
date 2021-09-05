package acme.features.api.anonymous.shout;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import acme.entities.shouts.Shout;
import acme.framework.controllers.AbstractRestController;
import acme.framework.entities.Anonymous;

@RestController
@RequestMapping("/api/anonymous/shout/")
public class ApiAnonymousShoutController extends AbstractRestController<Anonymous, Shout>{
	
	// Internal state ---------------------------------------------------------

	@Autowired
	protected ApiAnonymousShoutService	shoutService;
	
	// Constructors -----------------------------------------------------------

	@PostConstruct
	protected void initialise() {
		super.setService(this.shoutService);
	}
		
}
