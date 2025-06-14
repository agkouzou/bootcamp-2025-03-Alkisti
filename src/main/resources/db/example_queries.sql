-- select all users
select *
from users;

select *, price*stock as total_value
from products;

-- select users by criteria
select *
from users
where email = 'chris@ctrlspace.dev';

select *
from users
where password ='123456';


select *
from users
where password ='123456' and email = 'aaaa@ctrlspcae.dev';

select name
from users
where password ='123456' or email = 'nick@ctrlspace.dev';

-- Order by
select name
from users
where password ='123456' or email = 'nick@ctrlspace.dev'
order by name desc; -- asc

-- Group by
select password, count(1) as appearances_of_same_password
from users
GROUP BY password;

-- Having
select password, count(1) as appearances_of_same_password
from users
GROUP BY password
HAVING count(1) > 1;

-- Inner Join

select *
from users
    inner join orders o on users.id = o.user_id;

select *
from users u
    inner join orders o on u.id = o.user_id
where o.status = 'pending';

select u.email, count(1)
from users u
         inner join orders o on u.id = o.user_id
where status = 'completed'
group by u.email;

select u.email, count(1)
from users u
         inner join orders o on u.id = o.user_id
group by u.email;


select *
from users u
         inner join orders o on u.id = o.user_id
        inner join cart_items ci on o.id = ci.order_id;

select *
from users u
         inner join orders o on u.id = o.user_id
         inner join cart_items ci on o.id = ci.order_id
         inner join products p on p.id = ci.product_id;

select u.email, o.status, o.created_at, p.name, ci.quantity
from users u
         inner join orders o on u.id = o.user_id
         inner join cart_items ci on o.id = ci.order_id
         inner join products p on p.id = ci.product_id;


select o.id, o.status, u.email, count(1) as unique_barcode, sum(ci.quantity) as unique_items, sum(p.price * ci.quantity) as total_price
from users u
         inner join orders o on u.id = o.user_id
         inner join cart_items ci on o.id = ci.order_id
         inner join products p on p.id = ci.product_id
group by o.id, o.status, u.email;


-- Sub-queries

select order_totals.*, p2.name, ci.quantity * p2.price as subtotal_price
from (select o.id, o.status, u.email, count(1) as unique_barcode, sum(ci.quantity) as unique_items, sum(p.price * ci.quantity) as total_price
      from users u
               inner join orders o on u.id = o.user_id
               inner join cart_items ci on o.id = ci.order_id
               inner join products p on p.id = ci.product_id
      group by o.id, o.status, u.email) order_totals
    inner join cart_items ci on order_totals.id = ci.order_id
    inner join products p2 on p2.id = ci.product_id;


-- Left join
select o.id, o.status, u.email, count(ci.product_id) as unique_barcode, sum(COALESCE(ci.quantity, 0)) as unique_items, sum(COALESCE(p.price * ci.quantity, 0)) as total_price
from users u
         left join orders o on u.id = o.user_id
         left join cart_items ci on o.id = ci.order_id
         left join products p on p.id = ci.product_id
group by o.id, o.status, u.email























