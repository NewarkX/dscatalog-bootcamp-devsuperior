package com.devsuperior.dscatalog.services;

import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.RoleDTO;
import com.devsuperior.dscatalog.dto.UserDTO;
import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.Role;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.RoleRepository;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;


@Service	
public class UserService implements UserDetailsService {
	
	private static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//stream serve para transformar uma coleção em uma coleção de outro tipo,
	//o map vai percorrer a lista User passando todos os objetos para UserDTO,e no final
	//ele retorna uma lista do tipo stream entao e feita a conversao para lista 
	
	@Transactional(readOnly = true) //serve para manter a integridade do banco de dados,fazendo com que o jpa nao esteja aberto na camada de view
	public Page<UserDTO> findAllPaged( PageRequest pageRequest ){
		Page<User> list = repository.findAll(pageRequest);
		return list.map(x -> new UserDTO(x));
	}

	
	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		Optional<User> obj = repository.findById(id); //retorna um objeto do tipo optional que contem o objeto com id solicitado
		User entity =  obj.orElseThrow(() -> new EntityNotFoundException("Entity not found"));    //caso nao seja achado o categories no obj ele lança a excessao           // obj.get(); passa para o entity o objeto solicitado pelo id
		return new UserDTO(entity); //retorna um objetodto com a entidade
	}
	
	@Transactional
	public UserDTO insert(UserInsertDTO dto) {
		User entity = new User();
		copyDtoToEntity(dto,entity);
		entity.setPassword(passwordEncoder.encode(dto.getPassword()));
		entity = repository.save(entity);
		return new UserDTO(entity);
	}
	
	//findById ele efetiva o acesso ao banco de dados e traz os dados do objeto
	//getOne nao toca no banco de dados ele provisiona um objeto provisorio com aquele id so quando for salvar que ele
	//vai no banco de dados
	
	@Transactional
	public UserDTO update(Long id,UserUpdateDTO dto) {
		try { 
			User entity = repository.getOne(id);
			copyDtoToEntity(dto,entity);
			entity = repository.save(entity);
			return new UserDTO(entity); 
		} //se o id nao existir ele vai lancar excecao
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found" +id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		}
		catch( EmptyResultDataAccessException e ) {
			throw new ResourceNotFoundException("Id not found" +id);
		}
		catch( DataIntegrityViolationException e ) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(UserDTO dto, User entity) {
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());

		
		entity.getRoles().clear(); //limpa a lista da entidade
		for(RoleDTO roleDto : dto.getRoles()) {
			Role role = roleRepository.getOne(roleDto.getId()); //pega os ids de todos os elementos do dto joga na entidade category sem tocar no banco
			entity.getRoles().add(role);
		}
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repository.findByEmail(username);
		if(user == null) {
			logger.error("User not found:  " + username);
			throw new UsernameNotFoundException("email not found");
		}
		logger.info("User found: " + username);
		return user;
	}

}
