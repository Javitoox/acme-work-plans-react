
package acme.framework.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.Valid;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import acme.framework.components.HttpMethod;
import acme.framework.entities.UserRole;
import acme.framework.helpers.Assert;
import acme.framework.helpers.HttpMethodHelper;
import acme.framework.services.AbstractRestService;

@CrossOrigin(origins = MasterController.BASE_URL)
@RestController
public abstract class AbstractRestController<R extends UserRole, E> implements ApplicationContextAware {

	// Internal state ---------------------------------------------------------

	protected ConfigurableApplicationContext	context;
	
	protected Class<R>						roleClazz;
	protected Class<E>						entityClazz;
	
	@Autowired
	protected AbstractRestController<R, E>		self;

	@Autowired
	protected AbstractRestService<R, E>			service;

	// ApplicationContextAware interface --------------------------------------


	@Override
	public void setApplicationContext(final ApplicationContext context) throws BeansException {
		assert context != null;

		this.context = (ConfigurableApplicationContext) context;
	}

	// Service management ----------------------------------------------------

	public void setService(final AbstractRestService<R, E> service) {
		this.service = service;
	}

	// Transaction management ------------------------------------------------


	@Autowired
	protected PlatformTransactionManager	transactionManager;
	protected TransactionStatus				transactionStatus;


	protected void startTransaction() {
		TransactionDefinition transactionDefinition;

		transactionDefinition = new DefaultTransactionDefinition();
		this.transactionStatus = this.transactionManager.getTransaction(transactionDefinition);
	}

	protected void commitTransaction() {
		assert this.isTransactionActive();

		this.transactionManager.commit(this.transactionStatus);
		this.transactionStatus = null;
	}

	protected void rollbackTransaction() {
		assert this.isTransactionActive();

		this.transactionManager.rollback(this.transactionStatus);
		this.transactionStatus = null;
	}

	protected boolean isTransactionActive() {
		boolean result;

		result = this.transactionStatus != null && !this.transactionStatus.isCompleted();

		return result;
	}
	
	@SuppressWarnings("unchecked")
	protected AbstractRestController() {
		Class<?>[] types;

		types = GenericTypeResolver.resolveTypeArguments(this.getClass(), AbstractRestController.class);

		if (types == null || types.length != 2) {
			System.err.printf("I'm sorry, %s cannot be instantiated.%n", this.getClass().getName());
			System.err.printf("I can't resolve its generic types.%n");
			System.exit(1);
		}

		this.roleClazz = (Class<R>) types[0];
		this.entityClazz = (Class<E>) types[1];
	}	
	
	@RequestMapping(value = {
		"/list", "/create"
	}, method = {
		RequestMethod.GET, RequestMethod.POST
	})
	public ResponseEntity<?> handleRequest(@Valid @RequestBody(required = false) final E entity, final BindingResult result, 
		final HttpServletRequest servletRequest, final Authentication authentication, final Locale locale) {

		ResponseEntity<?> response = null;

		try {

			this.startTransaction();

			final String servletMethod = servletRequest.getMethod();
			final HttpMethod method = HttpMethodHelper.parse(servletMethod);

			Assert.state(this.service.authorise(authentication), locale, "default.error.not-authorised");

			switch (method) {
			case GET:
				response = this.self.doGet();
				break;
			case POST:
				response = this.self.doPost(entity, result);
				break;
			default:
				response = new ResponseEntity<>("Method not implemented", HttpStatus.NOT_IMPLEMENTED);
			}

			if (!response.getStatusCode().equals(HttpStatus.NON_AUTHORITATIVE_INFORMATION)) {
				this.commitTransaction();
				this.startTransaction();
				this.commitTransaction();
			} else {
				this.rollbackTransaction();
				this.startTransaction();
				this.commitTransaction();
			}

		} catch (final Throwable oops) {

			if (this.isTransactionActive()) {
				this.rollbackTransaction();
			}
			
			this.startTransaction();
			this.commitTransaction();
			
			response = new ResponseEntity<>("Not authorised", HttpStatus.UNAUTHORIZED);
		}

		return response;
	}
	
	@Transactional(TxType.MANDATORY)
	public ResponseEntity<?> doGet() {
		return ResponseEntity.ok(this.service.getAll());
	}
	
	@Transactional(TxType.MANDATORY)
	public ResponseEntity<?> doPost(final E entity, final BindingResult result) {
		final List<FieldError> errors = new ArrayList<>(); 
		this.service.validate(entity, errors);
		
		if (result.hasErrors() || !errors.isEmpty()) {
			final List<FieldError> totalErrors = new ArrayList<>();
			
			if (result.hasErrors()) {
				totalErrors.addAll(result.getFieldErrors());
			}
			
			if(!errors.isEmpty()) {
				totalErrors.addAll(errors);
			}
			
			return new ResponseEntity<>(totalErrors, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
		} else {
			this.service.save(entity);
			return new ResponseEntity<>("Successful creation", HttpStatus.CREATED);
		}
	}

}
