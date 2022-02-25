# Insert Testing user
admin:1
```sql
insert into "users"("id", "username", "email", "password")
values (nextval('user_sequence'), 'admin', 'admin@test.com', '$2a$10$qeKbMyrqQIIe3/BVJCdO5ubS61CHP5wNDFYhHjA02G/MnDzEPpQ3S');
insert into "user_roles"(user_id, role_id) VALUES (1000, 100);

```


