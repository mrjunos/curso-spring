package com.mrjunos.models.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.mrjunos.models.entity.Cliente;

public interface IClienteDAO extends PagingAndSortingRepository<Cliente, Long> {

}
