package com.devsuperior.dscatalog.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.repositories.CategoryRepository;

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
}
