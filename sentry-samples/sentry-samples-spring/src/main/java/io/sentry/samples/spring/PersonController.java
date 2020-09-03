package io.sentry.samples.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/person/")
public class PersonController {
  private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class);

  @GetMapping("{id}")
  Person person(@PathVariable Long id) {
    throw new IllegalArgumentException("Something went wrong [id=" + id + "]");
  }

  @PostMapping
  Person create(@RequestBody Person person) {
    LOGGER.warn("Creating person: {}", person);
    return person;
  }
}
