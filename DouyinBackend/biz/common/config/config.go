package config

import (
	"log"

	"github.com/spf13/viper"
)

type Config struct {
	Server   ServerConfig   `mapstructure:"server"`
	Database DatabaseConfig `mapstructure:"database"`
	JWT      JWTConfig      `mapstructure:"jwt"`
	Storage  StorageConfig  `mapstructure:"storage"`
}

type ServerConfig struct {
	Port           int    `mapstructure:"port"`
	Address        string `mapstructure:"address"`
	MaxRequestSize int    `mapstructure:"max_request_size"`
}

type DatabaseConfig struct {
	Driver string `mapstructure:"driver"`
	DSN    string `mapstructure:"dsn"`
}

type JWTConfig struct {
	Secret      string `mapstructure:"secret"`
	ExpireHours int    `mapstructure:"expire_hours"`
}

type StorageConfig struct {
	Type  string      `mapstructure:"type"`
	Local LocalConfig `mapstructure:"local"`
	OSS   OSSConfig   `mapstructure:"oss"`
}

type LocalConfig struct {
	VideoPath string `mapstructure:"video_path"`
	CoverPath string `mapstructure:"cover_path"`
	Domain    string `mapstructure:"domain"`
}

type OSSConfig struct {
	Endpoint        string `mapstructure:"endpoint"`
	AccessKeyID     string `mapstructure:"access_key_id"`
	AccessKeySecret string `mapstructure:"access_key_secret"`
	BucketName      string `mapstructure:"bucket_name"`
}

var GlobalConfig *Config

func Init() {
	viper.SetConfigFile("config/config.yaml")
	viper.SetConfigType("yaml")

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Error reading config file, %s", err)
	}

	GlobalConfig = &Config{}
	if err := viper.Unmarshal(GlobalConfig); err != nil {
		log.Fatalf("Unable to decode into struct, %v", err)
	}
	log.Println("Configuration loaded successfully")
}
