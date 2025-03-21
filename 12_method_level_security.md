1. Spring security not only supports the API level security, it also supports the method level security

2. Method can be in any layer : controller, service, repository, etc

3. @EnableMethodSecurity annotation is used to enable method level security
   1. prePostEnabled true is the default value which provide the latest annotations like PreAuthorize and PostAuthorize
   2. jsr250Enabled and seccuredEnabled are used to enable deplricated annotations like @Role and @Security

```
    @SpringBootApplication
    @EnableWebSecurity(debug = true)
    @EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true)
    public class EasyBankApplication {

        public static void main(String[] args) {
            SpringApplication.run(EasyBankApplication.class, args);  
        }

    }
```

4. There are 2 approach to apply the security
   1. Invocation authorization: Validates if someone can invoke a given method
   2. Filtering authorization: Validates the parameter that a method receives and the response body of a method

5. Invocation Authorization
   1. We use @PreAuthorize and @PostAuthorize to implement Invocation authorization
   2. Example of @PreAuthorize
   3. PreAuthorize first authorizes the user then implements the method where as the PostAuthorize first implements the method and then authorizes the usere
   4. we use PostAuthorize when we dont have enough information about the user before executing the method

```
@PreAuthorize("hasAuthority('viewLoans')")
@PreAuthorize("hasRole('admin')")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@PreAuthorize("#username == authentication.principal.username")
public Loan getLoanDetails(String username) {
    ...
}
```

```
@PostAuthorize("returnObject.username == authentication.principal.username")
public Loan getLoanDetails(String username) {
    ...
}
```

6. Filtering Authorization
   1. What kind of input a method can get and what kind of output a method can return
   2. We have to make sure that the method is accepting a collection object then only we can fitler the input parameters
   3. Annotation @PreFilter is used to filter the parameters
   4. Similarly we have to make sure that the method must return a collection object then only the filter can apply on the response
   5. Annotation @PostFilter is used to filter the response
   6. Filters removes the unwanted values and does not stops the execution

```
    @PostMapping("/contact")
    @PreFilter("filterObject.contactName != 'Test'")
    public Contact saveContactInquiryDetails(@RequestBody List<Contact> contacts) {
        if(!contacts.isEmpty()) {
            Contact contact = contacts.getFirst();
            contact.setContactId(getServiceReqNumber());
            contact.setCreateDt(new Date(System.currentTimeMillis()));
            return contactRepository.save(contact);
        }
        return null;
    }
```

```
    @PostMapping("/contact")
    @PostFilter("filterObject.contactName != 'Test'")
    public List<Contact> saveContactInquiryDetails(@RequestBody List<Contact> contacts) {
        List<Contact> returnContacts = new ArrayList<>();
        if(!contacts.isEmpty()) {
            Contact contact = contacts.getFirst();
            contact.setContactId(getServiceReqNumber());
            contact.setCreateDt(new Date(System.currentTimeMillis()));
            Contact savedContact =  contactRepository.save(contact);
            returnContacts.add(savedContact);
        }
        return returnContacts;
    }
```