package com.company.queueservice.util.feign;

import com.company.queueservice.util.messages.LevelUpMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "level-up-service")
public interface LevelUpClient {

    @RequestMapping(value = "/levelup/{id}", method = RequestMethod.PUT)
    LevelUpMessage updateLevelUp(@RequestBody LevelUpMessage levelUpMsg, @PathVariable int id);

    @RequestMapping(value = "/levelup/customer/{customerId}", method = RequestMethod.GET)
    LevelUpMessage getLevelUpByCustomerId(@PathVariable int customerId);

}
