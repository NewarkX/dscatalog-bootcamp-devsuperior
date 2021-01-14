package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@Service	
public class CategoryService {
	
	@Autowired
	private CategoryRepository repository;
	
	@Transactional(readOnly = true) //serve para manter a integridade do banco de dados,fazendo com que o jpa nao esteja aberto na camada de view
	public List<CategoryDTO> findAll(){
		List<Category> list = repository.findAll();
		return list.stream().map(x -> new CategoryDTO(x)).collect(Collectors.toList()); 
		//stream serve para transformar uma coleção em uma coleção de outro tipo,
		//o map vai percorrer a lista Category passando todos os objetos para CategoryDTO,e no final
		//ele retorna uma lista do tipo stream entao e feita a conversao para lista 
	}
	
	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> obj = repository.findById(id); //retorna um objeto do tipo optional que contem o objeto com id solicitado
		Category entity =  obj.orElseThrow(() -> new EntityNotFoundException("Entity not found"));    //caso nao seja achado o categories no obj ele lança a excessao           // obj.get(); passa para o entity o objeto solicitado pelo id
		return new CategoryDTO(entity); //retorna um objetodto com a entidade
	}
	
	@Transactional(readOnly = true)
	public CategoryDTO insert(CategoryDTO dto) {
		Category entity = new Category();
		entity.setName(dto.getName());
		entity = repository.save(entity);
		return new CategoryDTO(entity);
	}
	
	//findById ele efetiva o acesso ao banco de dados e traz os dados do objeto
	//getOne nao toca no banco de dados ele provisiona um objeto provisorio com aquele id so quando for salvar que ele
	//vai no banco de dados
	
	@Transactional
	public CategoryDTO update(Long id,CategoryDTO dto) {
		try { 
			Category entity = repository.getOne(id);
			entity.setName(dto.getName());
			entity = repository.save(entity);
			return new CategoryDTO(entity); 
		} //se o id nao existir ele vai lancar excecao
		catch(EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found" +id);
		}
	}
}
