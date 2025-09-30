set client_encoding = 'UTF-8';

-- create tablespace dbspace owner lab_user location 'D:\PG';
-- create database lab2pbz tablespace dbspace;
-- \c lab2pbz;

do $$
    begin
        if not exists (select from pg_catalog.pg_user where usename = 'lab_user') then
            create user lab_user with password 'password';
        end if;
    end
$$;

-- создаем схему и назначаем права
create schema if not exists lab2var10;
grant usage on schema lab2var10 to lab_user;
grant all privileges on all tables in schema lab2var10 to lab_user;
grant all privileges on all sequences in schema lab2var10 to lab_user;
grant all on schema lab2var10 to lab_user;
grant all privileges on database postgres to lab_user;
alter default privileges in schema lab2var10 grant all privileges on tables to lab_user;
alter default privileges in schema lab2var10 grant all privileges on sequences to lab_user;

-- таблица категорий товаров
create table if not exists categories (
    id bigint primary key,
    name varchar(100) not null unique
) /*partition by list (left(name, 1)) tablespace dbspace*/;

-- create table categories_part1 partition of categories for values in ('А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И');
-- create table categories_part2 partition of categories for values in ('Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т');
-- create table categories_part3 partition of categories for values in ('У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь');
-- create table categories_part4 partition of categories for values in ('Э', 'Ю', 'Я', 'A', 'B', 'C', 'D', 'E', 'F', 'G');
-- create table categories_part5 partition of categories for values in ('H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q');
-- create table categories_part6 partition of categories for values in ('R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0');
-- create table categories_part7 partition of categories for values in ('1', '2', '3', '4', '5', '6', '7', '8', '9');

-- таблица товаров
create table if not exists  products (
    id bigint primary key,
    code varchar(50) not null unique,
    name varchar(200) not null,
    category_id integer not null,
    manufacturer varchar(200) not null,
    foreign key (category_id) references categories(id) on delete cascade
) /*partition by range (category_id) tablespace dbspace*/;

-- create table products_part1 partition of products for values from (1) to (100);
-- create table products_part2 partition of products for values from (101) to (200);
-- create table products_part3 partition of products for values from (201) to (300);

-- таблица покупателей
create table if not exists  customers (
    id bigint primary key,
    name varchar(200) not null,
    address varchar(300) not null,
    is_legal_entity boolean not null,
    document_number varchar(100),
    document_series varchar(50),
    bank_name varchar(200),
    bank_account varchar(100)
) /*partition by range (id) tablespace dbspace*/;

-- create table customers_part1 partition of customers for values from (1) to (1000);
-- create table customers_part2 partition of customers for values from (1001) to (2000);
-- create table customers_part3 partition of customers for values from (2001) to (3000);

-- таблица регионов
create table if not exists  regions (
    id bigint primary key,
    name varchar(100) not null unique,
    country varchar(100) not null
) /*partition by list (country) tablespace dbspace*/;

-- create table regions_belarus partition of regions for values in ('Беларусь');
-- create table regions_cis partition of regions for values in ('Россия', 'Украина', 'Казахстан');
-- create table regions_other partition of regions for values in (default);

-- таблица населенных пунктов
create table if not exists  settlements (
    id bigint primary key,
    name varchar(100) not null,
    region_id integer not null,
    foreign key (region_id) references regions(id) on delete cascade
) /*partition by range (region_id) tablespace dbspace*/;

-- create table settlements_part1 partition of settlements for values from (1) to (50);
-- create table settlements_part2 partition of settlements for values from (51) to (100);
-- create table settlements_part3 partition of settlements for values from (101) to (150);

-- таблица накладных
create table if not exists  invoices (
    id bigint primary key,
    invoice_date date not null check (invoice_date <= CURRENT_DATE),
    customer_id integer not null,
    settlement_id integer not null,
    total_amount decimal(15,2) not null,
    enterprise varchar(200) not null,
    foreign key (customer_id) references customers(id) on delete cascade,
    foreign key (settlement_id) references settlements(id) on delete cascade
) /*partition by range (invoice_date) tablespace dbspace*/;

-- create table invoices_2023 partition of invoices for values from ('2023-01-01') to ('2024-01-01');
-- create table invoices_2024 partition of invoices for values from ('2024-01-01') to ('2025-01-01');
-- create table invoices_2025 partition of invoices for values from ('2025-01-01') to ('2026-01-01');

-- таблица позиций в накладной
create table if not exists  invoice_items (
    id bigint primary key,
    invoice_id integer not null,
    product_id integer not null,
    quantity numeric(38,0) not null check ( invoice_items.quantity > 0 ),
    price decimal(15,2) not null check ( price > 0 ),
    foreign key (invoice_id) references invoices(id) on delete cascade,
    foreign key (product_id) references products(id) on delete cascade
) /*partition by range (invoice_id) tablespace dbspace*/;

-- create table invoice_items_part1 partition of invoice_items for values from (1) to (1000);
-- create table invoice_items_part2 partition of invoice_items for values from (1001) to (2000);
-- create table invoice_items_part3 partition of invoice_items for values from (2001) to (3000);

-- таблица истории изменений цен
create table if not exists  price_history (
    id bigint primary key,
    product_id integer not null,
    change_date date not null check (change_date <= CURRENT_DATE),
    price decimal(15,2) not null check ( price_history.price > 0 ),
    foreign key (product_id) references products(id) on delete cascade
) /*partition by range (change_date) tablespace dbspace*/;

-- create table price_history_2023 partition of price_history for values from ('2023-01-01') to ('2024-01-01');
-- create table price_history_2024 partition of price_history for values from ('2024-01-01') to ('2025-01-01');
-- create table price_history_2025 partition of price_history for values from ('2025-01-01') to ('2026-01-01');

create index if not exists idx_products_category on products(category_id)/* tablespace dbspace*/;
create index if not exists idx_products_code on products(code)/* tablespace dbspace*/;
create index if not exists idx_customers_legal on customers(is_legal_entity)/* tablespace dbspace*/;
create index if not exists idx_customers_name on customers(name)/* tablespace dbspace*/;
create index if not exists idx_regions_country on regions(country)/* tablespace dbspace*/;
create index if not exists idx_settlements_region on settlements(region_id)/* tablespace dbspace*/;
create index if not exists idx_invoices_date on invoices(invoice_date)/* tablespace dbspace*/;
create index if not exists idx_invoices_customer on invoices(customer_id)/* tablespace dbspace*/;
create index if not exists idx_invoices_settlement on invoices(settlement_id)/* tablespace dbspace*/;
create index if not exists idx_invoice_items_invoice on invoice_items(invoice_id)/* tablespace dbspace*/;
create index if not exists idx_invoice_items_product on invoice_items(product_id)/* tablespace dbspace*/;
create index if not exists idx_price_history_product on price_history(product_id)/* tablespace dbspace*/;
create index if not exists idx_price_history_date on price_history(change_date)/* tablespace dbspace*/;

-- подготовленные выражения для вставки
prepare insert_category (varchar) as
    insert into categories (name) values ($1) on conflict do nothing;

prepare insert_product (varchar, varchar, integer, varchar) as
    insert into products (code, name, category_id, manufacturer) values ($1, $2, $3, $4) on conflict do nothing;

prepare insert_customer (varchar, varchar, boolean, varchar, varchar, varchar, varchar) as
    insert into customers (name, address, is_legal_entity, document_number, document_series, bank_name, bank_account)
    values ($1, $2, $3, $4, $5, $6, $7) on conflict do nothing;

prepare insert_region (varchar, varchar) as
    insert into regions (name, country) values ($1, $2) on conflict do nothing;

prepare insert_settlement (varchar, integer) as
    insert into settlements (name, region_id) values ($1, $2) on conflict do nothing;

prepare insert_invoice (date, integer, integer, decimal, varchar) as
    insert into invoices (invoice_date, customer_id, settlement_id, total_amount, enterprise)
    values ($1, $2, $3, $4, $5) on conflict do nothing;

prepare insert_invoice_item (integer, integer, integer, decimal) as
    insert into invoice_items (invoice_id, product_id, quantity, price)
    values ($1, $2, $3, $4) on conflict do nothing;

prepare insert_price_history (integer, date, decimal) as
    insert into price_history (product_id, change_date, price)
    values ($1, $2, $3) on conflict do nothing;

create or replace function add_product(
    p_code varchar,
    p_name varchar,
    p_category_id integer,
    p_manufacturer varchar
) returns void
as $$
begin
    insert into products (code, name, category_id, manufacturer)
    values (p_code, p_name, p_category_id, p_manufacturer)
    on conflict (code) do update set
        name = excluded.name,
        category_id = excluded.category_id,
        manufacturer = excluded.manufacturer;
end;
$$ language plpgsql;

create or replace function update_product(
    p_id integer,
    p_code varchar,
    p_name varchar,
    p_category_id integer,
    p_manufacturer varchar
) returns void
as $$
begin
    update products
    set code = p_code,
        name = p_name,
        category_id = p_category_id,
        manufacturer = p_manufacturer
    where id = p_id;
end;
$$ language plpgsql;

create or replace function delete_product(p_id integer) returns void as $$
begin
    delete from products where id = p_id;
end;
$$ language plpgsql;

create or replace function add_invoice(
    p_invoice_date date,
    p_customer_id integer,
    p_settlement_id integer,
    p_total_amount decimal,
    p_enterprise varchar
) returns integer
as $$
declare
    new_id integer;
begin
    insert into invoices (invoice_date, customer_id, settlement_id, total_amount, enterprise)
    values (p_invoice_date, p_customer_id, p_settlement_id, p_total_amount, p_enterprise)
    returning id into new_id;

    return new_id;
end;
$$ language plpgsql;

create or replace function update_invoice(
    p_id integer,
    p_invoice_date date,
    p_customer_id integer,
    p_settlement_id integer,
    p_total_amount decimal,
    p_enterprise varchar
) returns void
as $$
begin
    update invoices
    set invoice_date = p_invoice_date,
        customer_id = p_customer_id,
        settlement_id = p_settlement_id,
        total_amount = p_total_amount,
        enterprise = p_enterprise
    where id = p_id;
end;
$$ language plpgsql;

create or replace function delete_invoice(p_id integer) returns void as $$
begin
    delete from invoices where id = p_id;
end;
$$ language plpgsql;

create or replace function get_max_purchase_customers(p_date date)
    returns table (
        date_ date,
        customer_name varchar,
        address varchar,
        purchase_sum decimal
    )
as $$
begin
    return query
        select
            i.invoice_date as date_,
            c.name as customer_name,
            c.address as address,
            i.total_amount as purchase_sum
        from invoices i
                 join customers c on i.customer_id = c.id
        where i.invoice_date = p_date
          and i.total_amount = (
            select max(total_amount)
            from invoices
            where invoice_date = p_date
        );
end;
$$ language plpgsql;

create or replace function get_price_history(
    p_product_id integer,
    p_start_date date,
    p_end_date date
    ) returns table (
    product_name varchar,
    manufacturer varchar,
    change_date date,
    price decimal
    )
as $$
begin
    return query
        select
            p.name as product_name,
            p.manufacturer as manufacturer,
            ph.change_date as change_date,
            ph.price as price
        from price_history ph
                 join products p on ph.product_id = p.id
        where p.id = p_product_id
          and ph.change_date between p_start_date and p_end_date
        order by ph.change_date;
end;
$$ language plpgsql;

create or replace function get_categories()
    returns table (название_категории varchar)
as $$
begin
    return query
        select name from categories order by name;
end;
$$ language plpgsql;

create or replace function update_invoice_total()
    returns trigger as $$
declare
    target_invoice_id integer;
begin
    if tg_op = 'INSERT' then
        target_invoice_id := NEW.invoice_id;
    elsif tg_op = 'UPDATE' then
        IF NEW.invoice_id != OLD.invoice_id THEN
            update invoices
            set total_amount = (
                select coalesce(sum(quantity * price), 0)
                from invoice_items
                where invoice_id = OLD.invoice_id
            )
            where id = OLD.invoice_id;

            target_invoice_id := NEW.invoice_id;
        else
            target_invoice_id := NEW.invoice_id;
        end if;
    elsif tg_op = 'DELETE' then
        target_invoice_id := OLD.invoice_id;
    end if;

    update invoices
    set total_amount = (
        select coalesce(sum(quantity * price), 0)
        from invoice_items
        where invoice_id = target_invoice_id
    )
    where id = target_invoice_id;

    return coalesce(new, old);
end;
$$ language plpgsql;

create trigger trigger_update_invoice_total
    after insert or update or delete on invoice_items
    for each row execute function update_invoice_total();

create or replace function record_price_history()
    returns trigger as $$
begin
    if old.price is distinct from new.price then
        insert into price_history (product_id, change_date, price)
        values (new.product_id, current_date, new.price);
    end if;
    return new;
end;
$$ language plpgsql;

create trigger trigger_record_price_history
    after update on invoice_items
    for each row
    when (old.price is distinct from new.price)
execute function record_price_history();

-- create publication pub
--     for all tables
--     with (publish = 'insert', publish = 'update', publish = 'delete');
--
-- create subscription sub
--     connection 'host=0.0.0.0 port=5432 user=postgres dbname=lab1pbz'
--     publication pub;
--
-- grant connect on database lab2pbz to lab_user;
-- grant usage on schema lab2var10 to lab_user;
-- grant select, insert, update, delete on all tables in schema lab2var10 to lab_user;
-- grant usage on all sequences in schema lab2var10 to lab_user;


-- deallocate all;
-- deallocate insert_category;
-- deallocate insert_product;
-- deallocate insert_customer;
-- deallocate insert_region;
-- deallocate insert_settlement;
-- deallocate insert_invoice;
-- deallocate insert_invoice_item;
-- deallocate insert_price_history;
-- drop publication if exists lab_publication;
-- drop trigger if exists trigger_record_price_history on invoice_items;
-- drop trigger if exists trigger_update_invoice_total on invoice_items;
-- drop function if exists update_invoice_total();
-- drop function if exists record_price_history();
-- drop function if exists get_categories();
-- drop function if exists get_price_history(integer, date, date);
-- drop function if exists get_max_purchase_customers(date);
-- drop function if exists delete_invoice(integer);
-- drop function if exists update_invoice(integer, date, integer, integer, decimal);
-- drop function if exists add_invoice(date, integer, integer, decimal);
-- drop function if exists delete_product(integer);
-- drop function if exists update_product(integer, varchar, varchar, integer);
-- drop function if exists add_product(varchar, varchar, integer);
-- drop table if exists price_history_2023 cascade;
-- drop table if exists price_history_2024 cascade;
-- drop table if exists price_history_2025 cascade;
-- drop table if exists price_history cascade;
-- drop table if exists invoice_items_part1 cascade;
-- drop table if exists invoice_items_part2 cascade;
-- drop table if exists invoice_items_part3 cascade;
-- drop table if exists invoice_items cascade;
-- drop table if exists invoices_2023 cascade;
-- drop table if exists invoices_2024 cascade;
-- drop table if exists invoices_2025 cascade;
-- drop table if exists invoices cascade;
-- drop table if exists settlements_part1 cascade;
-- drop table if exists settlements_part2 cascade;
-- drop table if exists settlements_part3 cascade;
-- drop table if exists settlements cascade;
-- drop table if exists regions_belarus cascade;
-- drop table if exists regions_cis cascade;
-- drop table if exists regions_other cascade;
-- drop table if exists regions cascade;
-- drop table if exists customers_part1 cascade;
-- drop table if exists customers_part2 cascade;
-- drop table if exists customers_part3 cascade;
-- drop table if exists customers cascade;
-- drop table if exists products_part1 cascade;
-- drop table if exists products_part2 cascade;
-- drop table if exists products_part3 cascade;
-- drop table if exists products cascade;
-- drop table if exists categories_part1 cascade;
-- drop table if exists categories_part2 cascade;
-- drop table if exists categories_part3 cascade;
-- drop table if exists categories_part4 cascade;
-- drop table if exists categories_part5 cascade;
-- drop table if exists categories_part6 cascade;
-- drop table if exists categories_part7 cascade;
-- drop table if exists categories cascade;
-- drop schema if exists lab2var10 cascade;
-- drop user if exists lab_user;
-- drop database if exists lab1pbz;
-- drop tablespace if exists dbspace;

-- pg_dump -U postgres -h localhost -p 5432 lab2pbz > C:\backups\lab2pbz_backup.sql

-- execute insert_category('электроника');
-- execute insert_category('одежда');
-- execute insert_category('продукты питания');
-- execute insert_category('книги');
-- execute insert_category('мебель');
-- execute insert_product('el001', 'смартфон', 1, 'samsung');
-- execute insert_product('el002', 'ноутбук', 1, 'lenovo');
-- execute insert_product('cl001', 'футболка', 2, 'nike');
-- execute insert_product('fd001', 'хлеб', 3, 'хлебзавод №1');
-- execute insert_product('bk001', 'учебник sql', 4, 'питер');
-- execute insert_customer('иванов иван', 'ул. ленина, 10', false, '123456', 'AB', null, null);
-- execute insert_customer('ооо "романтика"', 'пр. мира, 25', true, '1234567890', null, 'белагропромбанк', 'BY00ALFA30120000000000000000');
-- execute insert_region('минская область', 'беларусь');
-- execute insert_region('гомельская область', 'беларусь');
-- execute insert_region('московская область', 'россия');
-- execute insert_settlement('минск', 1);
-- execute insert_settlement('гомель', 2);
-- execute insert_settlement('москва', 3);
-- execute insert_invoice('2024-05-15', 1, 1, 2500.00, 'магазин "техника"');
-- execute insert_invoice('2024-05-16', 2, 2, 15000.00, 'оптовый склад');
-- execute insert_invoice_item(1, 1, 2, 1200.00);
-- execute insert_invoice_item(1, 2, 1, 100.00);
-- execute insert_invoice_item(2, 3, 50, 300.00);
-- execute insert_price_history(1, '2024-01-01', 1100.00);
-- execute insert_price_history(1, '2024-03-01', 1150.00);
-- execute insert_price_history(1, '2024-05-01', 1200.00);
-- select * from categories;
-- select * from products where category_id = 1;
-- select * from customers where is_legal_entity = true;
-- select * from invoices where invoice_date = '2024-05-15';
-- select ii.*, p.name as product_name
-- from invoice_items ii
--          join products p on ii.product_id = p.id
-- where ii.invoice_id = 1;
-- select * from get_price_history(1, '2024-01-01', '2024-12-31');
-- select * from get_max_purchase_customers('2024-05-16');
-- select * from get_categories();
-- update invoice_items set price = 1250.00 where id = 1;
-- select * from invoices where id = 1;
-- execute insert_invoice_item(1, 5, 3, 500.00);
-- select * from invoices where id = 1;
-- delete from invoice_items where id = 3;
-- select * from invoices where id = 1;
-- select * from regions where country = 'беларусь';
-- select * from settlements where region_id = 1;
-- select invoice_date, sum(total_amount) as total_sales
-- from invoices
-- group by invoice_date
-- order by invoice_date;