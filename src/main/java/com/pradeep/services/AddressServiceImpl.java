package com.pradeep.services;

import com.pradeep.dto.AddressDto;
import com.pradeep.entities.AddressEntity;
import com.pradeep.entities.UserEntity;
import com.pradeep.repository.AddressRepository;
import com.pradeep.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Override
    public List<AddressDto> getAddresses(String userId) {
        List<AddressDto>returnValue = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserId(userId);
        if(optionalUserEntity.isEmpty()) {
            return returnValue;
        }
        Iterable<AddressEntity>addresses=addressRepository.findAllByUserDetails(optionalUserEntity.get());

        for(AddressEntity addressEntity:addresses) {
           // returnValue.add(modelMapper.map(addressEntity, AddressDto.class));
            AddressDto a=new AddressDto();
            BeanUtils.copyProperties(addressEntity,a);
            returnValue.add(a);
        }

        return returnValue;
    }

    @Override
    public AddressDto getAddress(String addressId) {
        AddressDto returnValue = null;
        AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
        if(addressEntity!=null) {
            returnValue=new AddressDto();
            BeanUtils.copyProperties(addressEntity,returnValue);
        }


        return returnValue;
    }
}
