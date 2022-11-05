package com.pradeep.services;

import com.pradeep.dto.AddressDto;
import com.pradeep.dto.UserDto;
import com.pradeep.entities.UserEntity;
import com.pradeep.exception.UserServiceException;
import com.pradeep.model.response.ErrorMessages;
import com.pradeep.repository.UserRepository;
import com.pradeep.util.AppUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUtils appUtils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDto createUser(UserDto user) {
        Optional<UserEntity> optionalUserEntity=userRepository.findByEmail(user.getEmail());
        if(optionalUserEntity.isPresent()){
            throw new RuntimeException("User exists");
        }
        for(int i=0; i<user.getAddresses().size();i++) {
            AddressDto address = user.getAddresses().get(i);
            address.setUserDetails(user);
            address.setAddressId(appUtils.generateUserId(30));
            user.getAddresses().set(i, address);
        }
        ModelMapper mapper=new ModelMapper();

      //  BeanUtils.copyProperties(userDto,userEntity);

        UserEntity userEntity = mapper.map(user,UserEntity.class);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setUserId(appUtils.generateUserId(30));
        userEntity.setEmailVerificationStatus(false);
        UserEntity storedUser=userRepository.save(userEntity);
       // UserDto returnValue=new UserDto();
        //BeanUtils.copyProperties(storedUser,returnValue);
        UserDto returnValue= mapper.map(storedUser,UserDto.class);
        return returnValue;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        UserEntity userEntity=userRepository.findByEmail(email).get();
        UserDto returnValue=new UserDto();
        BeanUtils.copyProperties(userEntity,returnValue);
        return returnValue;
    }

    @Override
    public UserDto getUserById(String id) {
       Optional<UserEntity>  optionalUserEntity=userRepository.findByUserId(id);
       if(optionalUserEntity.isEmpty()){
           throw new UsernameNotFoundException("User with id :: "+id+" not found");
       }
       UserDto userDto=new UserDto();
       BeanUtils.copyProperties(optionalUserEntity.get(),userDto);
       return userDto;
    }

    @Override
    public UserDto updateUser(String id, UserDto userDto) {
        UserDto returnValue= new UserDto();
        Optional<UserEntity>  optionalUserEntity = userRepository.findByUserId(id);
        if (optionalUserEntity.isEmpty()) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        UserEntity userEntity=optionalUserEntity.get();
        userEntity.setFirstName(userDto.getFirstName());
        userEntity.setLastName(userDto.getLastName());

        UserEntity updatedUserDetails = userRepository.save(userEntity);

        BeanUtils.copyProperties(updatedUserDetails, returnValue);

        return returnValue;
    }

    @Override
    public void deleteUser(String id) {
        Optional<UserEntity>  optionalUserEntity = userRepository.findByUserId(id);
        if (optionalUserEntity.isEmpty()) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        userRepository.delete(optionalUserEntity.get());
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto>returnValue = new ArrayList<>();

        //Avoiding pagination starts with 0:
        if(page>0) {
            page-=1;
        }

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);

        List<UserEntity>users = usersPage.getContent();

        for(UserEntity userEntity :users) {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity, userDto);
            returnValue.add(userDto);
        }

        return returnValue;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUserEntity=userRepository.findByEmail(email);
        if(optionalUserEntity.isEmpty()){
            throw new UsernameNotFoundException(email);
        }
        UserEntity userEntity=optionalUserEntity.get();
        return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),new ArrayList<>());
    }
}
