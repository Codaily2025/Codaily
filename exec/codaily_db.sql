--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-08-17 14:20:25

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5107 (class 1262 OID 16799)
-- Name: codaily_db; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE codaily_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Korean_Korea.949';


\connect codaily_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 16801)
-- Name: batch_process_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_process_log (
    id bigint NOT NULL,
    completed_at timestamp(6) without time zone,
    error_message character varying(1000),
    process_date date NOT NULL,
    processing_type character varying(50),
    project_id bigint NOT NULL,
    retry_count integer NOT NULL,
    started_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    CONSTRAINT batch_process_log_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying, 'SKIPPED'::character varying])::text[])))
);


--
-- TOC entry 217 (class 1259 OID 16800)
-- Name: batch_process_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_process_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5108 (class 0 OID 0)
-- Dependencies: 217
-- Name: batch_process_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.batch_process_log_id_seq OWNED BY public.batch_process_log.id;


--
-- TOC entry 220 (class 1259 OID 16811)
-- Name: chart_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chart_data (
    chart_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    data_json jsonb NOT NULL,
    granularity character varying(10),
    project_id bigint,
    retro_id bigint,
    type character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id bigint NOT NULL
);


--
-- TOC entry 219 (class 1259 OID 16810)
-- Name: chart_data_chart_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chart_data_chart_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5109 (class 0 OID 0)
-- Dependencies: 219
-- Name: chart_data_chart_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chart_data_chart_id_seq OWNED BY public.chart_data.chart_id;


--
-- TOC entry 221 (class 1259 OID 16819)
-- Name: code_commit_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.code_commit_feature (
    commit_id bigint NOT NULL,
    feature_name character varying(255)
);


--
-- TOC entry 223 (class 1259 OID 16823)
-- Name: code_commits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.code_commits (
    commit_id bigint NOT NULL,
    author character varying(255),
    commit_hash character varying(255),
    committed_at timestamp(6) without time zone,
    message character varying(255),
    feature_id bigint,
    project_id bigint NOT NULL
);


--
-- TOC entry 222 (class 1259 OID 16822)
-- Name: code_commits_commit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.code_commits_commit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5110 (class 0 OID 0)
-- Dependencies: 222
-- Name: code_commits_commit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.code_commits_commit_id_seq OWNED BY public.code_commits.commit_id;


--
-- TOC entry 225 (class 1259 OID 16832)
-- Name: code_review_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.code_review_item (
    item_id bigint NOT NULL,
    category character varying(255),
    file_path character varying(255),
    line_range character varying(255),
    message character varying(255),
    severity character varying(255),
    review_id bigint,
    feature_id bigint,
    checklist_id bigint NOT NULL
);


--
-- TOC entry 224 (class 1259 OID 16831)
-- Name: code_review_item_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.code_review_item_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5111 (class 0 OID 0)
-- Dependencies: 224
-- Name: code_review_item_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.code_review_item_item_id_seq OWNED BY public.code_review_item.item_id;


--
-- TOC entry 227 (class 1259 OID 16841)
-- Name: code_reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.code_reviews (
    review_id bigint NOT NULL,
    bug_risk character varying(255),
    complexity character varying(255),
    convention character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    performance character varying(255),
    quality_score double precision,
    refactor_suggestion character varying(255),
    security_risk character varying(255),
    summary text,
    feature_id bigint,
    project_id bigint NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 16840)
-- Name: code_reviews_review_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.code_reviews_review_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5112 (class 0 OID 0)
-- Dependencies: 226
-- Name: code_reviews_review_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.code_reviews_review_id_seq OWNED BY public.code_reviews.review_id;


--
-- TOC entry 229 (class 1259 OID 16850)
-- Name: daily_productivity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.daily_productivity (
    daily_id bigint NOT NULL,
    code_quality double precision NOT NULL,
    completed_features integer,
    created_at timestamp(6) without time zone,
    date date NOT NULL,
    productivity_score double precision NOT NULL,
    project_id bigint NOT NULL,
    total_commits integer,
    user_id bigint NOT NULL
);


--
-- TOC entry 228 (class 1259 OID 16849)
-- Name: daily_productivity_daily_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.daily_productivity_daily_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5113 (class 0 OID 0)
-- Dependencies: 228
-- Name: daily_productivity_daily_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.daily_productivity_daily_id_seq OWNED BY public.daily_productivity.daily_id;


--
-- TOC entry 231 (class 1259 OID 16857)
-- Name: days_of_week; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.days_of_week (
    days_id bigint NOT NULL,
    date_name character varying(20) NOT NULL,
    hours double precision NOT NULL,
    project_id bigint
);


--
-- TOC entry 230 (class 1259 OID 16856)
-- Name: days_of_week_days_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.days_of_week_days_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5114 (class 0 OID 0)
-- Dependencies: 230
-- Name: days_of_week_days_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.days_of_week_days_id_seq OWNED BY public.days_of_week.days_id;


--
-- TOC entry 233 (class 1259 OID 16864)
-- Name: feature_item_checklist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feature_item_checklist (
    checklist_id bigint NOT NULL,
    description text,
    done boolean NOT NULL,
    item text NOT NULL,
    feature_id bigint NOT NULL
);


--
-- TOC entry 232 (class 1259 OID 16863)
-- Name: feature_item_checklist_checklist_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.feature_item_checklist_checklist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5115 (class 0 OID 0)
-- Dependencies: 232
-- Name: feature_item_checklist_checklist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.feature_item_checklist_checklist_id_seq OWNED BY public.feature_item_checklist.checklist_id;


--
-- TOC entry 234 (class 1259 OID 16872)
-- Name: feature_item_checklist_filepaths; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feature_item_checklist_filepaths (
    checklist_id bigint NOT NULL,
    file_path character varying(255)
);


--
-- TOC entry 236 (class 1259 OID 16876)
-- Name: feature_item_schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feature_item_schedules (
    schedule_id bigint NOT NULL,
    allocated_hours double precision NOT NULL,
    schedule_date date NOT NULL,
    within_project_period boolean NOT NULL,
    feature_id bigint NOT NULL
);


--
-- TOC entry 235 (class 1259 OID 16875)
-- Name: feature_item_schedules_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.feature_item_schedules_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5116 (class 0 OID 0)
-- Dependencies: 235
-- Name: feature_item_schedules_schedule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.feature_item_schedules_schedule_id_seq OWNED BY public.feature_item_schedules.schedule_id;


--
-- TOC entry 238 (class 1259 OID 16883)
-- Name: feature_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.feature_items (
    feature_id bigint NOT NULL,
    category character varying(50),
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    description text,
    estimated_time double precision,
    field character varying(50),
    is_custom boolean,
    is_reduced boolean NOT NULL,
    is_selected boolean NOT NULL,
    priority_level integer,
    remaining_time double precision,
    status character varying(50) NOT NULL,
    title text NOT NULL,
    updated_at timestamp(6) without time zone,
    parent_feature_id bigint,
    project_id bigint NOT NULL,
    spec_id bigint
);


--
-- TOC entry 237 (class 1259 OID 16882)
-- Name: feature_items_feature_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.feature_items_feature_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5117 (class 0 OID 0)
-- Dependencies: 237
-- Name: feature_items_feature_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.feature_items_feature_id_seq OWNED BY public.feature_items.feature_id;


--
-- TOC entry 240 (class 1259 OID 16892)
-- Name: productivity_metrics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.productivity_metrics (
    metric_id bigint NOT NULL,
    code_quality_score double precision NOT NULL,
    code_quality_weight double precision NOT NULL,
    commit_frequency_score double precision NOT NULL,
    commit_weight double precision NOT NULL,
    commits integer NOT NULL,
    completed_tasks integer NOT NULL,
    created_at timestamp(6) without time zone,
    date date NOT NULL,
    personal_average double precision NOT NULL,
    productivity_score double precision NOT NULL,
    project_average double precision NOT NULL,
    project_id bigint NOT NULL,
    task_completion_score double precision NOT NULL,
    task_weight double precision NOT NULL,
    trend character varying(255),
    user_id bigint NOT NULL,
    CONSTRAINT productivity_metrics_trend_check CHECK (((trend)::text = ANY ((ARRAY['IMPROVING'::character varying, 'DECLINING'::character varying, 'STABLE'::character varying])::text[])))
);


--
-- TOC entry 239 (class 1259 OID 16891)
-- Name: productivity_metrics_metric_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.productivity_metrics_metric_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5118 (class 0 OID 0)
-- Dependencies: 239
-- Name: productivity_metrics_metric_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.productivity_metrics_metric_id_seq OWNED BY public.productivity_metrics.metric_id;


--
-- TOC entry 242 (class 1259 OID 16900)
-- Name: project_repositories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.project_repositories (
    repo_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    repo_name character varying(255) NOT NULL,
    repo_url text NOT NULL,
    project_id bigint NOT NULL
);


--
-- TOC entry 241 (class 1259 OID 16899)
-- Name: project_repositories_repo_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.project_repositories_repo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5119 (class 0 OID 0)
-- Dependencies: 241
-- Name: project_repositories_repo_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.project_repositories_repo_id_seq OWNED BY public.project_repositories.repo_id;


--
-- TOC entry 244 (class 1259 OID 16909)
-- Name: projects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.projects (
    project_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    end_date date,
    start_date date,
    status character varying(20),
    title character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    spec_id bigint,
    user_id bigint NOT NULL,
    CONSTRAINT projects_status_check CHECK (((status)::text = ANY ((ARRAY['TODO'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- TOC entry 243 (class 1259 OID 16908)
-- Name: projects_project_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.projects_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5120 (class 0 OID 0)
-- Dependencies: 243
-- Name: projects_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.projects_project_id_seq OWNED BY public.projects.project_id;


--
-- TOC entry 246 (class 1259 OID 16919)
-- Name: retrospectives; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.retrospectives (
    retro_id bigint NOT NULL,
    content text,
    created_at timestamp(6) without time zone NOT NULL,
    date date NOT NULL,
    summary_json jsonb NOT NULL,
    trigger_type character varying(20) NOT NULL,
    user_comment text,
    project_id bigint NOT NULL,
    CONSTRAINT retrospectives_trigger_type_check CHECK (((trigger_type)::text = ANY ((ARRAY['AUTO'::character varying, 'MANUAL'::character varying])::text[])))
);


--
-- TOC entry 245 (class 1259 OID 16918)
-- Name: retrospectives_retro_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.retrospectives_retro_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5121 (class 0 OID 0)
-- Dependencies: 245
-- Name: retrospectives_retro_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.retrospectives_retro_id_seq OWNED BY public.retrospectives.retro_id;


--
-- TOC entry 248 (class 1259 OID 16929)
-- Name: schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schedules (
    schedule_id bigint NOT NULL,
    scheduled_date date NOT NULL,
    project_id bigint NOT NULL
);


--
-- TOC entry 247 (class 1259 OID 16928)
-- Name: schedules_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.schedules_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5122 (class 0 OID 0)
-- Dependencies: 247
-- Name: schedules_schedule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.schedules_schedule_id_seq OWNED BY public.schedules.schedule_id;


--
-- TOC entry 250 (class 1259 OID 16936)
-- Name: spec_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.spec_documents (
    spec_id bigint NOT NULL,
    content text,
    created_at timestamp(6) without time zone NOT NULL,
    format character varying(20),
    priority_level integer,
    title character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL
);


--
-- TOC entry 249 (class 1259 OID 16935)
-- Name: spec_documents_spec_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.spec_documents_spec_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5123 (class 0 OID 0)
-- Dependencies: 249
-- Name: spec_documents_spec_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.spec_documents_spec_id_seq OWNED BY public.spec_documents.spec_id;


--
-- TOC entry 252 (class 1259 OID 16945)
-- Name: tech_stacks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tech_stacks (
    tech_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_custom boolean NOT NULL,
    name character varying(100) NOT NULL,
    user_id bigint NOT NULL
);


--
-- TOC entry 251 (class 1259 OID 16944)
-- Name: tech_stacks_tech_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tech_stacks_tech_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5124 (class 0 OID 0)
-- Dependencies: 251
-- Name: tech_stacks_tech_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tech_stacks_tech_id_seq OWNED BY public.tech_stacks.tech_id;


--
-- TOC entry 254 (class 1259 OID 16952)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    github_access_token text,
    github_account character varying(255),
    github_profile_url text,
    github_scope text,
    nickname character varying(50) NOT NULL,
    password character varying(255),
    profile_image text,
    role character varying(255) NOT NULL,
    social_access_token text,
    social_id character varying(255),
    social_provider character varying(20) NOT NULL,
    social_refresh_token text,
    token_expired_at timestamp(6) without time zone,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


--
-- TOC entry 253 (class 1259 OID 16951)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 5125 (class 0 OID 0)
-- Dependencies: 253
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 4835 (class 2604 OID 16804)
-- Name: batch_process_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_process_log ALTER COLUMN id SET DEFAULT nextval('public.batch_process_log_id_seq'::regclass);


--
-- TOC entry 4836 (class 2604 OID 16814)
-- Name: chart_data chart_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chart_data ALTER COLUMN chart_id SET DEFAULT nextval('public.chart_data_chart_id_seq'::regclass);


--
-- TOC entry 4837 (class 2604 OID 16826)
-- Name: code_commits commit_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_commits ALTER COLUMN commit_id SET DEFAULT nextval('public.code_commits_commit_id_seq'::regclass);


--
-- TOC entry 4838 (class 2604 OID 16835)
-- Name: code_review_item item_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_review_item ALTER COLUMN item_id SET DEFAULT nextval('public.code_review_item_item_id_seq'::regclass);


--
-- TOC entry 4839 (class 2604 OID 16844)
-- Name: code_reviews review_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_reviews ALTER COLUMN review_id SET DEFAULT nextval('public.code_reviews_review_id_seq'::regclass);


--
-- TOC entry 4840 (class 2604 OID 16853)
-- Name: daily_productivity daily_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.daily_productivity ALTER COLUMN daily_id SET DEFAULT nextval('public.daily_productivity_daily_id_seq'::regclass);


--
-- TOC entry 4841 (class 2604 OID 16860)
-- Name: days_of_week days_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.days_of_week ALTER COLUMN days_id SET DEFAULT nextval('public.days_of_week_days_id_seq'::regclass);


--
-- TOC entry 4842 (class 2604 OID 16867)
-- Name: feature_item_checklist checklist_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_checklist ALTER COLUMN checklist_id SET DEFAULT nextval('public.feature_item_checklist_checklist_id_seq'::regclass);


--
-- TOC entry 4843 (class 2604 OID 16879)
-- Name: feature_item_schedules schedule_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_schedules ALTER COLUMN schedule_id SET DEFAULT nextval('public.feature_item_schedules_schedule_id_seq'::regclass);


--
-- TOC entry 4844 (class 2604 OID 16886)
-- Name: feature_items feature_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_items ALTER COLUMN feature_id SET DEFAULT nextval('public.feature_items_feature_id_seq'::regclass);


--
-- TOC entry 4845 (class 2604 OID 16895)
-- Name: productivity_metrics metric_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productivity_metrics ALTER COLUMN metric_id SET DEFAULT nextval('public.productivity_metrics_metric_id_seq'::regclass);


--
-- TOC entry 4846 (class 2604 OID 16903)
-- Name: project_repositories repo_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_repositories ALTER COLUMN repo_id SET DEFAULT nextval('public.project_repositories_repo_id_seq'::regclass);


--
-- TOC entry 4847 (class 2604 OID 16912)
-- Name: projects project_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects ALTER COLUMN project_id SET DEFAULT nextval('public.projects_project_id_seq'::regclass);


--
-- TOC entry 4848 (class 2604 OID 16922)
-- Name: retrospectives retro_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retrospectives ALTER COLUMN retro_id SET DEFAULT nextval('public.retrospectives_retro_id_seq'::regclass);


--
-- TOC entry 4849 (class 2604 OID 16932)
-- Name: schedules schedule_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedules ALTER COLUMN schedule_id SET DEFAULT nextval('public.schedules_schedule_id_seq'::regclass);


--
-- TOC entry 4850 (class 2604 OID 16939)
-- Name: spec_documents spec_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spec_documents ALTER COLUMN spec_id SET DEFAULT nextval('public.spec_documents_spec_id_seq'::regclass);


--
-- TOC entry 4851 (class 2604 OID 16948)
-- Name: tech_stacks tech_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_stacks ALTER COLUMN tech_id SET DEFAULT nextval('public.tech_stacks_tech_id_seq'::regclass);


--
-- TOC entry 4852 (class 2604 OID 16955)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 5065 (class 0 OID 16801)
-- Dependencies: 218
-- Data for Name: batch_process_log; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5067 (class 0 OID 16811)
-- Dependencies: 220
-- Data for Name: chart_data; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5068 (class 0 OID 16819)
-- Dependencies: 221
-- Data for Name: code_commit_feature; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5070 (class 0 OID 16823)
-- Dependencies: 223
-- Data for Name: code_commits; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5072 (class 0 OID 16832)
-- Dependencies: 225
-- Data for Name: code_review_item; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5074 (class 0 OID 16841)
-- Dependencies: 227
-- Data for Name: code_reviews; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5076 (class 0 OID 16850)
-- Dependencies: 229
-- Data for Name: daily_productivity; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5078 (class 0 OID 16857)
-- Dependencies: 231
-- Data for Name: days_of_week; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5080 (class 0 OID 16864)
-- Dependencies: 233
-- Data for Name: feature_item_checklist; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5081 (class 0 OID 16872)
-- Dependencies: 234
-- Data for Name: feature_item_checklist_filepaths; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5083 (class 0 OID 16876)
-- Dependencies: 236
-- Data for Name: feature_item_schedules; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5085 (class 0 OID 16883)
-- Dependencies: 238
-- Data for Name: feature_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5087 (class 0 OID 16892)
-- Dependencies: 240
-- Data for Name: productivity_metrics; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5089 (class 0 OID 16900)
-- Dependencies: 242
-- Data for Name: project_repositories; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5091 (class 0 OID 16909)
-- Dependencies: 244
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5093 (class 0 OID 16919)
-- Dependencies: 246
-- Data for Name: retrospectives; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5095 (class 0 OID 16929)
-- Dependencies: 248
-- Data for Name: schedules; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5097 (class 0 OID 16936)
-- Dependencies: 250
-- Data for Name: spec_documents; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5099 (class 0 OID 16945)
-- Dependencies: 252
-- Data for Name: tech_stacks; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5101 (class 0 OID 16952)
-- Dependencies: 254
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 5126 (class 0 OID 0)
-- Dependencies: 217
-- Name: batch_process_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.batch_process_log_id_seq', 1, false);


--
-- TOC entry 5127 (class 0 OID 0)
-- Dependencies: 219
-- Name: chart_data_chart_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.chart_data_chart_id_seq', 1, false);


--
-- TOC entry 5128 (class 0 OID 0)
-- Dependencies: 222
-- Name: code_commits_commit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.code_commits_commit_id_seq', 1, false);


--
-- TOC entry 5129 (class 0 OID 0)
-- Dependencies: 224
-- Name: code_review_item_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.code_review_item_item_id_seq', 1, false);


--
-- TOC entry 5130 (class 0 OID 0)
-- Dependencies: 226
-- Name: code_reviews_review_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.code_reviews_review_id_seq', 1, false);


--
-- TOC entry 5131 (class 0 OID 0)
-- Dependencies: 228
-- Name: daily_productivity_daily_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.daily_productivity_daily_id_seq', 1, false);


--
-- TOC entry 5132 (class 0 OID 0)
-- Dependencies: 230
-- Name: days_of_week_days_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.days_of_week_days_id_seq', 1, false);


--
-- TOC entry 5133 (class 0 OID 0)
-- Dependencies: 232
-- Name: feature_item_checklist_checklist_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.feature_item_checklist_checklist_id_seq', 1, false);


--
-- TOC entry 5134 (class 0 OID 0)
-- Dependencies: 235
-- Name: feature_item_schedules_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.feature_item_schedules_schedule_id_seq', 1, false);


--
-- TOC entry 5135 (class 0 OID 0)
-- Dependencies: 237
-- Name: feature_items_feature_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.feature_items_feature_id_seq', 1, false);


--
-- TOC entry 5136 (class 0 OID 0)
-- Dependencies: 239
-- Name: productivity_metrics_metric_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.productivity_metrics_metric_id_seq', 1, false);


--
-- TOC entry 5137 (class 0 OID 0)
-- Dependencies: 241
-- Name: project_repositories_repo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.project_repositories_repo_id_seq', 1, false);


--
-- TOC entry 5138 (class 0 OID 0)
-- Dependencies: 243
-- Name: projects_project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.projects_project_id_seq', 1, false);


--
-- TOC entry 5139 (class 0 OID 0)
-- Dependencies: 245
-- Name: retrospectives_retro_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.retrospectives_retro_id_seq', 1, false);


--
-- TOC entry 5140 (class 0 OID 0)
-- Dependencies: 247
-- Name: schedules_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.schedules_schedule_id_seq', 1, false);


--
-- TOC entry 5141 (class 0 OID 0)
-- Dependencies: 249
-- Name: spec_documents_spec_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.spec_documents_spec_id_seq', 1, false);


--
-- TOC entry 5142 (class 0 OID 0)
-- Dependencies: 251
-- Name: tech_stacks_tech_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tech_stacks_tech_id_seq', 1, false);


--
-- TOC entry 5143 (class 0 OID 0)
-- Dependencies: 253
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_user_id_seq', 1, false);


--
-- TOC entry 4859 (class 2606 OID 16809)
-- Name: batch_process_log batch_process_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_process_log
    ADD CONSTRAINT batch_process_log_pkey PRIMARY KEY (id);


--
-- TOC entry 4861 (class 2606 OID 16818)
-- Name: chart_data chart_data_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chart_data
    ADD CONSTRAINT chart_data_pkey PRIMARY KEY (chart_id);


--
-- TOC entry 4863 (class 2606 OID 16830)
-- Name: code_commits code_commits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_commits
    ADD CONSTRAINT code_commits_pkey PRIMARY KEY (commit_id);


--
-- TOC entry 4865 (class 2606 OID 16839)
-- Name: code_review_item code_review_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_review_item
    ADD CONSTRAINT code_review_item_pkey PRIMARY KEY (item_id);


--
-- TOC entry 4867 (class 2606 OID 16848)
-- Name: code_reviews code_reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_reviews
    ADD CONSTRAINT code_reviews_pkey PRIMARY KEY (review_id);


--
-- TOC entry 4869 (class 2606 OID 16855)
-- Name: daily_productivity daily_productivity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.daily_productivity
    ADD CONSTRAINT daily_productivity_pkey PRIMARY KEY (daily_id);


--
-- TOC entry 4873 (class 2606 OID 16862)
-- Name: days_of_week days_of_week_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.days_of_week
    ADD CONSTRAINT days_of_week_pkey PRIMARY KEY (days_id);


--
-- TOC entry 4875 (class 2606 OID 16871)
-- Name: feature_item_checklist feature_item_checklist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_checklist
    ADD CONSTRAINT feature_item_checklist_pkey PRIMARY KEY (checklist_id);


--
-- TOC entry 4877 (class 2606 OID 16881)
-- Name: feature_item_schedules feature_item_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_schedules
    ADD CONSTRAINT feature_item_schedules_pkey PRIMARY KEY (schedule_id);


--
-- TOC entry 4879 (class 2606 OID 16890)
-- Name: feature_items feature_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_items
    ADD CONSTRAINT feature_items_pkey PRIMARY KEY (feature_id);


--
-- TOC entry 4881 (class 2606 OID 16898)
-- Name: productivity_metrics productivity_metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productivity_metrics
    ADD CONSTRAINT productivity_metrics_pkey PRIMARY KEY (metric_id);


--
-- TOC entry 4883 (class 2606 OID 16907)
-- Name: project_repositories project_repositories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_repositories
    ADD CONSTRAINT project_repositories_pkey PRIMARY KEY (repo_id);


--
-- TOC entry 4885 (class 2606 OID 16917)
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (project_id);


--
-- TOC entry 4887 (class 2606 OID 16927)
-- Name: retrospectives retrospectives_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retrospectives
    ADD CONSTRAINT retrospectives_pkey PRIMARY KEY (retro_id);


--
-- TOC entry 4891 (class 2606 OID 16934)
-- Name: schedules schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedules
    ADD CONSTRAINT schedules_pkey PRIMARY KEY (schedule_id);


--
-- TOC entry 4893 (class 2606 OID 16943)
-- Name: spec_documents spec_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.spec_documents
    ADD CONSTRAINT spec_documents_pkey PRIMARY KEY (spec_id);


--
-- TOC entry 4895 (class 2606 OID 16950)
-- Name: tech_stacks tech_stacks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_stacks
    ADD CONSTRAINT tech_stacks_pkey PRIMARY KEY (tech_id);


--
-- TOC entry 4871 (class 2606 OID 16962)
-- Name: daily_productivity ukk0987ice3f6418k8yflr4963p; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.daily_productivity
    ADD CONSTRAINT ukk0987ice3f6418k8yflr4963p UNIQUE (user_id, project_id, date);


--
-- TOC entry 4889 (class 2606 OID 16964)
-- Name: retrospectives ukolkxuwhh4381prbox1neu9m5s; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retrospectives
    ADD CONSTRAINT ukolkxuwhh4381prbox1neu9m5s UNIQUE (project_id, date);


--
-- TOC entry 4897 (class 2606 OID 16960)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4914 (class 2606 OID 17045)
-- Name: projects fk18b1sehfrq4v4kidn34sjqc56; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fk18b1sehfrq4v4kidn34sjqc56 FOREIGN KEY (spec_id) REFERENCES public.spec_documents(spec_id);


--
-- TOC entry 4904 (class 2606 OID 16995)
-- Name: code_reviews fk2c7yl0hyiiyxeobdcqmna1mlp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_reviews
    ADD CONSTRAINT fk2c7yl0hyiiyxeobdcqmna1mlp FOREIGN KEY (feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4905 (class 2606 OID 17000)
-- Name: code_reviews fk3815kr33h8erqk59nfufk02u0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_reviews
    ADD CONSTRAINT fk3815kr33h8erqk59nfufk02u0 FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4918 (class 2606 OID 17065)
-- Name: tech_stacks fk3ai56jkmvwtvg16bik2og8nnd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tech_stacks
    ADD CONSTRAINT fk3ai56jkmvwtvg16bik2og8nnd FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4901 (class 2606 OID 16990)
-- Name: code_review_item fk3d5hhrkinaw1641l7fly85yt1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_review_item
    ADD CONSTRAINT fk3d5hhrkinaw1641l7fly85yt1 FOREIGN KEY (checklist_id) REFERENCES public.feature_item_checklist(checklist_id);


--
-- TOC entry 4917 (class 2606 OID 17060)
-- Name: schedules fk6mu7v3088u6koha8kolxocotn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedules
    ADD CONSTRAINT fk6mu7v3088u6koha8kolxocotn FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4913 (class 2606 OID 17040)
-- Name: project_repositories fk7si845ni6lfnitgctkei33p6j; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.project_repositories
    ADD CONSTRAINT fk7si845ni6lfnitgctkei33p6j FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4899 (class 2606 OID 16970)
-- Name: code_commits fkbw0bxugrjgygd918bbrorp7ym; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_commits
    ADD CONSTRAINT fkbw0bxugrjgygd918bbrorp7ym FOREIGN KEY (feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4916 (class 2606 OID 17055)
-- Name: retrospectives fkcfwh1dn75i508xla1c701c2x6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retrospectives
    ADD CONSTRAINT fkcfwh1dn75i508xla1c701c2x6 FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4902 (class 2606 OID 16985)
-- Name: code_review_item fkdgk1n41k6l1i3syrk7gyr88in; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_review_item
    ADD CONSTRAINT fkdgk1n41k6l1i3syrk7gyr88in FOREIGN KEY (feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4906 (class 2606 OID 17005)
-- Name: days_of_week fkffaloilbx5kif08njhmg3gqnr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.days_of_week
    ADD CONSTRAINT fkffaloilbx5kif08njhmg3gqnr FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4915 (class 2606 OID 17050)
-- Name: projects fkhswfwa3ga88vxv1pmboss6jhm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT fkhswfwa3ga88vxv1pmboss6jhm FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4900 (class 2606 OID 16975)
-- Name: code_commits fkjrcivp8k1tpuqu4ysc4fjn4n8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_commits
    ADD CONSTRAINT fkjrcivp8k1tpuqu4ysc4fjn4n8 FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4898 (class 2606 OID 16965)
-- Name: code_commit_feature fkmt1b7slgdkrgnktlt3yno7moy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_commit_feature
    ADD CONSTRAINT fkmt1b7slgdkrgnktlt3yno7moy FOREIGN KEY (commit_id) REFERENCES public.code_commits(commit_id);


--
-- TOC entry 4910 (class 2606 OID 17025)
-- Name: feature_items fknrk39tf43xtnd0560113w2m12; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_items
    ADD CONSTRAINT fknrk39tf43xtnd0560113w2m12 FOREIGN KEY (parent_feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4911 (class 2606 OID 17030)
-- Name: feature_items fkqj37uv8fgn5l2qsx8fi1h52c0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_items
    ADD CONSTRAINT fkqj37uv8fgn5l2qsx8fi1h52c0 FOREIGN KEY (project_id) REFERENCES public.projects(project_id);


--
-- TOC entry 4909 (class 2606 OID 17020)
-- Name: feature_item_schedules fkqk0kb57f2hhus2cwrw93vdxh3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_schedules
    ADD CONSTRAINT fkqk0kb57f2hhus2cwrw93vdxh3 FOREIGN KEY (feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4908 (class 2606 OID 17015)
-- Name: feature_item_checklist_filepaths fkrm9epfs6lnhgnyais40u1dued; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_checklist_filepaths
    ADD CONSTRAINT fkrm9epfs6lnhgnyais40u1dued FOREIGN KEY (checklist_id) REFERENCES public.feature_item_checklist(checklist_id);


--
-- TOC entry 4903 (class 2606 OID 16980)
-- Name: code_review_item fkru96nr1ppjga33veyegpd4uwb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.code_review_item
    ADD CONSTRAINT fkru96nr1ppjga33veyegpd4uwb FOREIGN KEY (review_id) REFERENCES public.code_reviews(review_id);


--
-- TOC entry 4907 (class 2606 OID 17010)
-- Name: feature_item_checklist fks7172m4d6yo9c4bcd8wvwpti6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_item_checklist
    ADD CONSTRAINT fks7172m4d6yo9c4bcd8wvwpti6 FOREIGN KEY (feature_id) REFERENCES public.feature_items(feature_id);


--
-- TOC entry 4912 (class 2606 OID 17035)
-- Name: feature_items fktjf06oo8r369rqqfmamx9c3uc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.feature_items
    ADD CONSTRAINT fktjf06oo8r369rqqfmamx9c3uc FOREIGN KEY (spec_id) REFERENCES public.spec_documents(spec_id);


-- Completed on 2025-08-17 14:20:26

--
-- PostgreSQL database dump complete
--

