create table if not exists currencies(
    id integer primary key autoincrement,
    code TEXT not null unique,
    fullname TEXT not null,
    sign TEXT not null
);

create table if not exists exchange_rate(
	id integer primary key autoincrement,
	base_currency_id integer not null,
	target_currency_id integer not null,
	rate real not null,
	unique(base_currency_id, target_currency_id),
	foreign key(base_currency_id) references currencies(id) on delete cascade,
	foreign key(target_currency_id) references currencies(id) on delete cascade
);