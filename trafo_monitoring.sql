PGDMP                         }            trafo_monitoring    10.0    10.0 >    P           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                       false            Q           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                       false                        2615    2200    public    SCHEMA        CREATE SCHEMA public;
    DROP SCHEMA public;
             postgres    false            R           0    0    SCHEMA public    COMMENT     6   COMMENT ON SCHEMA public IS 'standard public schema';
                  postgres    false    3            �            1255    17385    update_updated_at_column()    FUNCTION     �   CREATE FUNCTION update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;
 1   DROP FUNCTION public.update_updated_at_column();
       public       postgres    false    3            �            1259    17386    flyway_schema_history    TABLE     �  CREATE TABLE flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);
 )   DROP TABLE public.flyway_schema_history;
       public         postgres    false    3            �            1259    17358    notifications    TABLE       CREATE TABLE notifications (
    id bigint NOT NULL,
    user_id bigint,
    title character varying(200) NOT NULL,
    message text NOT NULL,
    type character varying(20) DEFAULT 'INFO'::character varying,
    read_status boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['INFO'::character varying, 'WARNING'::character varying, 'ERROR'::character varying, 'SUCCESS'::character varying])::text[])))
);
 !   DROP TABLE public.notifications;
       public         postgres    false    3            �            1259    17356    notifications_id_seq    SEQUENCE     v   CREATE SEQUENCE notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.notifications_id_seq;
       public       postgres    false    3    205            S           0    0    notifications_id_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE notifications_id_seq OWNED BY notifications.id;
            public       postgres    false    204            �            1259    17312    status_history    TABLE     '  CREATE TABLE status_history (
    id bigint NOT NULL,
    transformer_id bigint NOT NULL,
    old_status character varying(20),
    new_status character varying(20) NOT NULL,
    changed_by bigint NOT NULL,
    changed_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    notes text
);
 "   DROP TABLE public.status_history;
       public         postgres    false    3            �            1259    17310    status_history_id_seq    SEQUENCE     w   CREATE SEQUENCE status_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 ,   DROP SEQUENCE public.status_history_id_seq;
       public       postgres    false    201    3            T           0    0    status_history_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE status_history_id_seq OWNED BY status_history.id;
            public       postgres    false    200            �            1259    17287    transformers    TABLE     �  CREATE TABLE transformers (
    id bigint NOT NULL,
    nama_trafo character varying(100) NOT NULL,
    kode_trafo character varying(50) NOT NULL,
    lokasi character varying(200) NOT NULL,
    kapasitas_kva numeric(10,2) NOT NULL,
    tegangan_kv numeric(8,2) NOT NULL,
    status character varying(20) NOT NULL,
    priority character varying(10) DEFAULT 'MEDIUM'::character varying,
    progress integer DEFAULT 0,
    pic_engineer bigint,
    tanggal_mulai date,
    tanggal_target date,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT transformers_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying, 'CRITICAL'::character varying])::text[]))),
    CONSTRAINT transformers_progress_check CHECK (((progress >= 0) AND (progress <= 100))),
    CONSTRAINT transformers_status_check CHECK (((status)::text = ANY ((ARRAY['OPERATIONAL'::character varying, 'MAINTENANCE'::character varying, 'FAULT'::character varying, 'OFFLINE'::character varying])::text[])))
);
     DROP TABLE public.transformers;
       public         postgres    false    3            �            1259    17285    transformers_id_seq    SEQUENCE     u   CREATE SEQUENCE transformers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.transformers_id_seq;
       public       postgres    false    3    199            U           0    0    transformers_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE transformers_id_seq OWNED BY transformers.id;
            public       postgres    false    198            �            1259    17272    users    TABLE     1  CREATE TABLE users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    email character varying(100) NOT NULL,
    role character varying(20) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'ENGINEER'::character varying, 'OPERATOR'::character varying, 'VIEWER'::character varying])::text[])))
);
    DROP TABLE public.users;
       public         postgres    false    3            �            1259    17270    users_id_seq    SEQUENCE     n   CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 #   DROP SEQUENCE public.users_id_seq;
       public       postgres    false    3    197            V           0    0    users_id_seq    SEQUENCE OWNED BY     /   ALTER SEQUENCE users_id_seq OWNED BY users.id;
            public       postgres    false    196            �            1259    17334    work_orders    TABLE     G  CREATE TABLE work_orders (
    id bigint NOT NULL,
    transformer_id bigint NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    assigned_to bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    completed_at timestamp without time zone,
    CONSTRAINT work_orders_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);
    DROP TABLE public.work_orders;
       public         postgres    false    3            �            1259    17332    work_orders_id_seq    SEQUENCE     t   CREATE SEQUENCE work_orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 )   DROP SEQUENCE public.work_orders_id_seq;
       public       postgres    false    203    3            W           0    0    work_orders_id_seq    SEQUENCE OWNED BY     ;   ALTER SEQUENCE work_orders_id_seq OWNED BY work_orders.id;
            public       postgres    false    202            �
           2604    17361    notifications id    DEFAULT     f   ALTER TABLE ONLY notifications ALTER COLUMN id SET DEFAULT nextval('notifications_id_seq'::regclass);
 ?   ALTER TABLE public.notifications ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    205    204    205            �
           2604    17315    status_history id    DEFAULT     h   ALTER TABLE ONLY status_history ALTER COLUMN id SET DEFAULT nextval('status_history_id_seq'::regclass);
 @   ALTER TABLE public.status_history ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    200    201    201            �
           2604    17290    transformers id    DEFAULT     d   ALTER TABLE ONLY transformers ALTER COLUMN id SET DEFAULT nextval('transformers_id_seq'::regclass);
 >   ALTER TABLE public.transformers ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    198    199    199            �
           2604    17275    users id    DEFAULT     V   ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    196    197    197            �
           2604    17337    work_orders id    DEFAULT     b   ALTER TABLE ONLY work_orders ALTER COLUMN id SET DEFAULT nextval('work_orders_id_seq'::regclass);
 =   ALTER TABLE public.work_orders ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    202    203    203            M          0    17386    flyway_schema_history 
   TABLE DATA               �   COPY flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
    public       postgres    false    206   �P       L          0    17358    notifications 
   TABLE DATA               \   COPY notifications (id, user_id, title, message, type, read_status, created_at) FROM stdin;
    public       postgres    false    205   ]Q       H          0    17312    status_history 
   TABLE DATA               l   COPY status_history (id, transformer_id, old_status, new_status, changed_by, changed_at, notes) FROM stdin;
    public       postgres    false    201   IR       F          0    17287    transformers 
   TABLE DATA               �   COPY transformers (id, nama_trafo, kode_trafo, lokasi, kapasitas_kva, tegangan_kv, status, priority, progress, pic_engineer, tanggal_mulai, tanggal_target, notes, created_at, updated_at) FROM stdin;
    public       postgres    false    199   �R       D          0    17272    users 
   TABLE DATA               U   COPY users (id, username, password, email, role, created_at, updated_at) FROM stdin;
    public       postgres    false    197   VT       J          0    17334    work_orders 
   TABLE DATA               u   COPY work_orders (id, transformer_id, title, description, status, assigned_to, created_at, completed_at) FROM stdin;
    public       postgres    false    203   +U       X           0    0    notifications_id_seq    SEQUENCE SET     ;   SELECT pg_catalog.setval('notifications_id_seq', 3, true);
            public       postgres    false    204            Y           0    0    status_history_id_seq    SEQUENCE SET     <   SELECT pg_catalog.setval('status_history_id_seq', 2, true);
            public       postgres    false    200            Z           0    0    transformers_id_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('transformers_id_seq', 4, true);
            public       postgres    false    198            [           0    0    users_id_seq    SEQUENCE SET     3   SELECT pg_catalog.setval('users_id_seq', 4, true);
            public       postgres    false    196            \           0    0    work_orders_id_seq    SEQUENCE SET     9   SELECT pg_catalog.setval('work_orders_id_seq', 3, true);
            public       postgres    false    202            �
           2606    17394 .   flyway_schema_history flyway_schema_history_pk 
   CONSTRAINT     q   ALTER TABLE ONLY flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);
 X   ALTER TABLE ONLY public.flyway_schema_history DROP CONSTRAINT flyway_schema_history_pk;
       public         postgres    false    206            �
           2606    17370     notifications notifications_pkey 
   CONSTRAINT     W   ALTER TABLE ONLY notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);
 J   ALTER TABLE ONLY public.notifications DROP CONSTRAINT notifications_pkey;
       public         postgres    false    205            �
           2606    17321 "   status_history status_history_pkey 
   CONSTRAINT     Y   ALTER TABLE ONLY status_history
    ADD CONSTRAINT status_history_pkey PRIMARY KEY (id);
 L   ALTER TABLE ONLY public.status_history DROP CONSTRAINT status_history_pkey;
       public         postgres    false    201            �
           2606    17304 (   transformers transformers_kode_trafo_key 
   CONSTRAINT     b   ALTER TABLE ONLY transformers
    ADD CONSTRAINT transformers_kode_trafo_key UNIQUE (kode_trafo);
 R   ALTER TABLE ONLY public.transformers DROP CONSTRAINT transformers_kode_trafo_key;
       public         postgres    false    199            �
           2606    17302    transformers transformers_pkey 
   CONSTRAINT     U   ALTER TABLE ONLY transformers
    ADD CONSTRAINT transformers_pkey PRIMARY KEY (id);
 H   ALTER TABLE ONLY public.transformers DROP CONSTRAINT transformers_pkey;
       public         postgres    false    199            �
           2606    17284    users users_email_key 
   CONSTRAINT     J   ALTER TABLE ONLY users
    ADD CONSTRAINT users_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public.users DROP CONSTRAINT users_email_key;
       public         postgres    false    197            �
           2606    17280    users users_pkey 
   CONSTRAINT     G   ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public         postgres    false    197            �
           2606    17282    users users_username_key 
   CONSTRAINT     P   ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);
 B   ALTER TABLE ONLY public.users DROP CONSTRAINT users_username_key;
       public         postgres    false    197            �
           2606    17345    work_orders work_orders_pkey 
   CONSTRAINT     S   ALTER TABLE ONLY work_orders
    ADD CONSTRAINT work_orders_pkey PRIMARY KEY (id);
 F   ALTER TABLE ONLY public.work_orders DROP CONSTRAINT work_orders_pkey;
       public         postgres    false    203            �
           1259    17395    flyway_schema_history_s_idx    INDEX     Y   CREATE INDEX flyway_schema_history_s_idx ON flyway_schema_history USING btree (success);
 /   DROP INDEX public.flyway_schema_history_s_idx;
       public         postgres    false    206            �
           1259    17384    idx_notifications_read_status    INDEX     W   CREATE INDEX idx_notifications_read_status ON notifications USING btree (read_status);
 1   DROP INDEX public.idx_notifications_read_status;
       public         postgres    false    205            �
           1259    17383    idx_notifications_user_id    INDEX     O   CREATE INDEX idx_notifications_user_id ON notifications USING btree (user_id);
 -   DROP INDEX public.idx_notifications_user_id;
       public         postgres    false    205            �
           1259    17379    idx_status_history_changed_at    INDEX     W   CREATE INDEX idx_status_history_changed_at ON status_history USING btree (changed_at);
 1   DROP INDEX public.idx_status_history_changed_at;
       public         postgres    false    201            �
           1259    17378 !   idx_status_history_transformer_id    INDEX     _   CREATE INDEX idx_status_history_transformer_id ON status_history USING btree (transformer_id);
 5   DROP INDEX public.idx_status_history_transformer_id;
       public         postgres    false    201            �
           1259    17377    idx_transformers_pic_engineer    INDEX     W   CREATE INDEX idx_transformers_pic_engineer ON transformers USING btree (pic_engineer);
 1   DROP INDEX public.idx_transformers_pic_engineer;
       public         postgres    false    199            �
           1259    17376    idx_transformers_status    INDEX     K   CREATE INDEX idx_transformers_status ON transformers USING btree (status);
 +   DROP INDEX public.idx_transformers_status;
       public         postgres    false    199            �
           1259    17381    idx_work_orders_assigned_to    INDEX     S   CREATE INDEX idx_work_orders_assigned_to ON work_orders USING btree (assigned_to);
 /   DROP INDEX public.idx_work_orders_assigned_to;
       public         postgres    false    203            �
           1259    17382    idx_work_orders_status    INDEX     I   CREATE INDEX idx_work_orders_status ON work_orders USING btree (status);
 *   DROP INDEX public.idx_work_orders_status;
       public         postgres    false    203            �
           1259    17380    idx_work_orders_transformer_id    INDEX     Y   CREATE INDEX idx_work_orders_transformer_id ON work_orders USING btree (transformer_id);
 2   DROP INDEX public.idx_work_orders_transformer_id;
       public         postgres    false    203            �
           2606    17371 (   notifications notifications_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
 R   ALTER TABLE ONLY public.notifications DROP CONSTRAINT notifications_user_id_fkey;
       public       postgres    false    205    197    2731            �
           2606    17327 -   status_history status_history_changed_by_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY status_history
    ADD CONSTRAINT status_history_changed_by_fkey FOREIGN KEY (changed_by) REFERENCES users(id);
 W   ALTER TABLE ONLY public.status_history DROP CONSTRAINT status_history_changed_by_fkey;
       public       postgres    false    2731    201    197            �
           2606    17322 1   status_history status_history_transformer_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY status_history
    ADD CONSTRAINT status_history_transformer_id_fkey FOREIGN KEY (transformer_id) REFERENCES transformers(id) ON DELETE CASCADE;
 [   ALTER TABLE ONLY public.status_history DROP CONSTRAINT status_history_transformer_id_fkey;
       public       postgres    false    201    2739    199            �
           2606    17305 +   transformers transformers_pic_engineer_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY transformers
    ADD CONSTRAINT transformers_pic_engineer_fkey FOREIGN KEY (pic_engineer) REFERENCES users(id);
 U   ALTER TABLE ONLY public.transformers DROP CONSTRAINT transformers_pic_engineer_fkey;
       public       postgres    false    199    2731    197            �
           2606    17351 (   work_orders work_orders_assigned_to_fkey    FK CONSTRAINT     }   ALTER TABLE ONLY work_orders
    ADD CONSTRAINT work_orders_assigned_to_fkey FOREIGN KEY (assigned_to) REFERENCES users(id);
 R   ALTER TABLE ONLY public.work_orders DROP CONSTRAINT work_orders_assigned_to_fkey;
       public       postgres    false    203    197    2731            �
           2606    17346 +   work_orders work_orders_transformer_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY work_orders
    ADD CONSTRAINT work_orders_transformer_id_fkey FOREIGN KEY (transformer_id) REFERENCES transformers(id) ON DELETE CASCADE;
 U   ALTER TABLE ONLY public.work_orders DROP CONSTRAINT work_orders_transformer_id_fkey;
       public       postgres    false    199    2739    203            M   �   x���A
�0 ϛW��f�JD�
�Г E��&P�}-�Z�g`�2~1�K��Pv2z�x�Cqn˺j��k�Z7o�B:y{r�)F)�>�� # ���2|TNA{�a���7���7��o�
�>x��IBF2J���S�3�ޫ�9K      L   �   x�}��J�@�ϓ��H��ڀ�El��
E�6�v����Iŷw)���|��U�a�'�q��MJ��4�[���.�D�����������g���j���e[*U��k{A+�,U]*��Z����F��fYh0��<�����sփ�x&��<�����.�5���Ly�b_�ol��nT��NB6g��u�@B�]9A��e�9kk{{���(����^�      H   �   x�}ͽ
�0@���)�-���-H
�����%$-�1y���v��T `>��4��9j���{m�]�5��ǦǮ����-����<�;��n��KHlp�4?� /��6��{J侮�q	7_9-��vqv-c*I0+      F   `  x����r�0���)� �I�ة7TĨ��x�eGR�@c'�v���Ja(3=�C�O���/(J5�]p��r��^`@a����]��.!E��
s�U��HD}bǀ���.�m�R�I�5Z�h���d�6t	u�_Kv��*�+el�/Y�7�{u�%�%#�x��1��h��ы��s�q"r(%f7�ɍ�u���v�@q��4L�d�8��C����Ȗ���D1He�uXWF*\�"�
�=��)���v�J��gPcs����g�t�S>��kOC]�ԅs-�̡���Li�|g�����%�Qf�U�vn���<8����b��Uap&�8�=��3�m4�F^�S��q~ '�Ѝ      D   �   x����
�@ ����-�*3��UB�^B"Aڈ3ٔ9&]��[⪅��|
z��q!#(��]��0���q���\r�*O��:��~_P��9�U���X�u��B�x�(���P��@m��u��MG3�L��x�X�����Fb?�	Ig�4 Z���6f&�dGRo�̵����{�k���uH6���*I�dI��      J   �   x����n�@D��+�DH��[��� "=F�,p�U�e�����rk�����Amg/�x"Q�J�0�XJ����Ɛ�zA�zG:]��[�-)67�(��iȋ��.�:=�!��D��$��qmvf�3�*6�K��KD��#ͽ�\�<y������yn����زg7|߻�$�y`UZ�"[���6�5����/���?����atܱNrg���/O�[����YA�$^f�     