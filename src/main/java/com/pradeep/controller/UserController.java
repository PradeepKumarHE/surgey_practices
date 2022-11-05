package com.pradeep.controller;

import com.pradeep.dto.AddressDto;
import com.pradeep.dto.UserDto;
import com.pradeep.enums.RequestOperationName;
import com.pradeep.enums.RequestOperationStatus;
import com.pradeep.exception.UserServiceException;
import com.pradeep.model.request.UserDetailsRequestModel;
import com.pradeep.model.response.AddressRest;
import com.pradeep.model.response.ErrorMessages;
import com.pradeep.model.response.OperationStatusModel;
import com.pradeep.model.response.UserRest;
import com.pradeep.services.AddressService;
import com.pradeep.services.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @PostMapping
    public UserRest createUser(@RequestBody UserDetailsRequestModel userdetails) throws Exception {
        if(userdetails.getEmail().isEmpty()){
            throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
        }

       // UserDto userDto=new UserDto();
       // BeanUtils.copyProperties(userdetails,userDto);

        ModelMapper modelMapper = new ModelMapper();
        UserDto userDto = modelMapper.map(userdetails, UserDto.class);

        UserDto createdUser=userService.createUser(userDto);
        UserRest userRest=modelMapper.map(createdUser, UserRest.class);
        return userRest;
    }

    @GetMapping(path = "/{id}")
    public UserRest getUserById(@PathVariable String id){
        UserDto userDto=userService.getUserById(id);
        UserRest userRest=new UserRest();
        BeanUtils.copyProperties(userDto,userRest);
        return userRest;
    }

    @PutMapping(path="/{id}")
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {

        UserRest returnValue=new UserRest();
        UserDto userDto=new UserDto();

        // Copies sourceObject into a target object:
        BeanUtils.copyProperties(userDetails, userDto);

        UserDto updatedUser=userService.updateUser(id, userDto);

        BeanUtils.copyProperties(updatedUser,returnValue );

        return returnValue;
    }

    @DeleteMapping(path="/{id}")
    public OperationStatusModel deleteUser(@PathVariable String id) {

        OperationStatusModel returnValue= new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.DELETE.name());

        userService.deleteUser(id);

        returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());

        return returnValue;
    }

    @GetMapping
    public List<UserRest> getUsers(@RequestParam(value = "page",defaultValue = "0") int page,
                                   @RequestParam(value = "limit",defaultValue = "25") int limit){
        List<UserRest> userRestList=new ArrayList<UserRest>();
        List<UserDto> userDtos=userService.getUsers(page,limit);
        for (UserDto userDto:userDtos){
            UserRest userRest=new UserRest();
            BeanUtils.copyProperties(userDto,userRest );
            userRestList.add(userRest);
        }

        //BeanUtils.copyProperties(userDto,userRest);
        return userRestList;
    }


    @GetMapping(path = "/{id}/addresses")
    public List<AddressRest> getUserAddresses(@PathVariable String id) {

        List<AddressRest>  returnValue = new ArrayList<>();

        List<AddressDto> addressesDto = addressService.getAddresses(id);

        if (addressesDto != null && !addressesDto.isEmpty()) {
            Type listType = new TypeToken<List<AddressRest>>() {
            }.getType();
            returnValue = new ModelMapper().map(addressesDto, listType);
        }


        return returnValue;

    }

    @GetMapping(path = "/{userId}/addresses/{addressId}")
    public EntityModel<AddressRest> getUserAddress(@PathVariable String userId,@PathVariable String addressId) {
        AddressDto addressesDto = addressService.getAddress(addressId);
        AddressRest returnValue=new AddressRest();
        BeanUtils.copyProperties(addressesDto,returnValue);

        Link userLink=WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");


        /* Link userAddressLink=WebMvcLinkBuilder.linkTo(UserController.class)
                .slash(userId)
                .slash("addresses")
                .withRel("addresses");*/

        Link userAddressLink=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddresses(userId))
                //.slash(userId)
                //.slash("addresses")
                .withRel("addresses");


        /*Link selfLink=WebMvcLinkBuilder.linkTo(UserController.class)
                .slash(userId).
                slash("addresses")
                .slash(addressId).
                withSelfRel();*/

        Link selfLink=WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getUserAddress(userId,addressId)).

              /*  .slash(userId).
                slash("addresses")
                .slash(addressId).*/
                withSelfRel();


       /* returnValue.add(userLink);
        returnValue.add(userAddressLink);
        returnValue.add(selfLink);*/



        return EntityModel.of(returnValue, Arrays.asList(userLink,userAddressLink,selfLink));
    }
}
